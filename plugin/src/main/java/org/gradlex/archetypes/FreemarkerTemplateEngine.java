package org.gradlex.archetypes;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FreemarkerTemplateEngine implements TemplateEngine {

    private Configuration configuration;

    @Override
    public void initialize(File templateRepoCloneLocation) throws Exception {
        this.configuration = loadFreemarkerConfiguration(templateRepoCloneLocation);
    }

    private static Configuration loadFreemarkerConfiguration(File templateRepoCloneLocation) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDirectoryForTemplateLoading(templateRepoCloneLocation);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        return cfg;
    }

    @Override
    public void processTemplates(Logger logger, File targetLocation, File templateRepoCloneLocation, Map<String, Object> data) throws Exception {
        for (Object f : FileUtils.listFiles(templateRepoCloneLocation, null, true)) {
            File file = (File) f;
            if (file.isFile()) {
                URI fileUri = file.toURI();
                URI baseUri = templateRepoCloneLocation.toURI();
                String relativePath = baseUri.relativize(fileUri).getPath();
                if (!isIgnored(relativePath)) {
                    processTemplate(logger, targetLocation, data, file, baseUri, templateRepoCloneLocation);
                }
            }
        }
    }

    private void processTemplate(Logger logger, File targetDir, Map<String, Object> data, File file, URI baseUri, File localRepoDir) throws IOException, TemplateException {
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        boolean hasMetadata = false;
        int endLine = -1;
        if (lines.size() > 0 && lines.get(0).startsWith("<#GradleTemplate>")) {
            hasMetadata = true;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).equals("</#GradleTemplate>"))  {
                    endLine = i;
                    break;
                }
            }
        }
        if (hasMetadata && endLine == -1) {
            throw new RuntimeException("No </#GradleTemplate> tag found for <#GradleTemplate>");
        }

        HashMap<String, Object> finalData = new HashMap<>(data);
        finalData.put("gradleVersion", new TemplateGradleVersion());
        if (hasMetadata) {
            String paramTemplateName = file.getName() + ".GradleTemplate.params.txt.template"; // TODO use relative path to avoid template caching
            File paramsTemplateFile = new File(localRepoDir, paramTemplateName); // TODO check for collisions
            if (paramsTemplateFile.exists()) {
                paramsTemplateFile.delete();
                FileUtils.touch(paramsTemplateFile);
            }
            FileUtils.writeLines(paramsTemplateFile, lines.subList(1, endLine));

            Template paramsTemplate = configuration.getTemplate(paramTemplateName);
            StringWriter writer = new StringWriter();
            paramsTemplate.process(finalData, writer, null);
            logger.lifecycle(file.getName() + ": " + writer.toString());
            Properties props = new Properties();
            props.load(new StringReader(writer.toString()));
            logger.lifecycle("Properties for " + file.getName() + ": " + props);
            for (Object key : props.keySet()) {
                finalData.put((String) key, props.get(key));
            }
            paramsTemplateFile.delete();

            FileUtils.writeLines(file, lines.subList(endLine + 1, lines.size()));
        }

        Object skip = finalData.get("skip");
        if (skip == null || !"true".equals(skip)) {
            String targetFileName = file.getName().substring(0, file.getName().length() - 9);
            URI templateUri = file.toURI();
            URI generatedFileUri = new File(file.getParentFile(), targetFileName).toURI();
            String fileName = (String) finalData.get("file");
            String generatedFileRelativePath =  fileName == null ? baseUri.relativize(generatedFileUri).getPath() : fileName;
            String templateRelativePath = baseUri.relativize(templateUri).getPath();
            Template template = configuration.getTemplate(templateRelativePath);
            File targetFile = new File(targetDir, generatedFileRelativePath);
            targetFile.getParentFile().mkdirs();
            FileUtils.touch(targetFile);
            Writer out = new OutputStreamWriter(new FileOutputStream(targetFile));
            template.process(finalData, out, null);
        }
    }

    private static boolean isIgnored(String relativePath) {
        return relativePath.startsWith(".git") || relativePath.startsWith(".gradle") || Arrays.asList("gradlew", "gradlew.bat").contains(relativePath) || relativePath.equals("templateOptions.json");
    }
}
