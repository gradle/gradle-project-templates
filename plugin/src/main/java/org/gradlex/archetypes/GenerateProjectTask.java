package org.gradlex.archetypes;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.tasks.userinput.UserInputHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.buildinit.tasks.InitBuild;

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

public abstract class GenerateProjectTask extends InitBuild {

    private final Directory projectDir = getProject().getLayout().getProjectDirectory();

    @Input
    @Option(option = "template", description = "Specifies the template to use")
    public abstract Property<String> getTemplate();


    @Override
    public void setupProjectLayout() {
        // hack superclass to do nothing here
    }

    @TaskAction
    public void initTaskAction() throws Exception {
        materializeTemplate(getTemplate().get());
    }

    private void materializeTemplate(String url) throws Exception {
        File localRepoDir = getProject().getLayout().getBuildDirectory().dir("tmp/gitClone").get().getAsFile();
        File targetDir = projectDir.getAsFile();
        getLogger().info("Cloning template repository. Source: " + url + ", destination: " + targetDir.getAbsolutePath() + ".");
        TemplateRepository.from(url).clone(localRepoDir);
        Configuration configuration = loadFreemarkerConfiguration(localRepoDir);
        File optionsFile = new File(localRepoDir, "templateOptions.json");
        Map<String, Object> data = loadTemplateData(optionsFile);
        processTemplates(targetDir, localRepoDir, configuration, data);
        FileUtils.deleteDirectory(localRepoDir);
    }

    private static Configuration loadFreemarkerConfiguration(File localRepoDir) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDirectoryForTemplateLoading(localRepoDir);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        return cfg;
    }

    private Map<String, Object> loadTemplateData(File optionsFile) throws IOException {
        // TODO no options file
        Descriptor descriptor = Descriptor.read(optionsFile);
        return  new Interrogator(getServices().get(UserInputHandler.class)).askQuestions(descriptor.getQuestions());
    }

    private void processTemplates(File targetDir, File localRepoDir, Configuration freemarkerConfig, Map<String, Object> data) throws IOException, TemplateException {
        for (Object f : FileUtils.listFiles(localRepoDir, null, true)) {
            File file = (File) f;
            if (file.isFile()) {
                URI fileUri = file.toURI();
                URI baseUri = localRepoDir.toURI();
                String relativePath = baseUri.relativize(fileUri).getPath();
                if (!isIgnored(relativePath)) {
                    processTemplate(targetDir, freemarkerConfig, data, file, baseUri, localRepoDir);
                }
            }
        }
    }

    private void processTemplate(File targetDir, Configuration freemarkerConfig, Map<String, Object> data, File file, URI baseUri, File localRepoDir) throws IOException, TemplateException {
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

            Template paramsTemplate = freemarkerConfig.getTemplate(paramTemplateName);
            StringWriter writer = new StringWriter();
            paramsTemplate.process(finalData, writer, null);
            getLogger().lifecycle(file.getName() + ": " + writer.toString());
            Properties props = new Properties();
            props.load(new StringReader(writer.toString()));
            getLogger().lifecycle("Properties for " + file.getName() + ": " + props);
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
            Template template = freemarkerConfig.getTemplate(templateRelativePath);
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

