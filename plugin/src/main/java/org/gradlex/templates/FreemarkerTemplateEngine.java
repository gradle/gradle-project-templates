package org.gradlex.templates;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
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
    public Map<String, Object> processTemplateMetadata(File templateRepoCloneLocation, File file, List<String> metadata, Map<String, Object> data) throws Exception {
        String paramTemplateName = file.getName() + ".GradleTemplate"; // TODO use relative path to avoid template caching
        File paramsTemplateFile = new File(templateRepoCloneLocation, paramTemplateName); // TODO check for collisions
        if (paramsTemplateFile.exists()) {
            paramsTemplateFile.delete();
            FileUtils.touch(paramsTemplateFile);
        }
        FileUtils.writeLines(paramsTemplateFile, metadata);

        Template paramsTemplate = configuration.getTemplate(paramTemplateName);
        StringWriter writer = new StringWriter();

        Map<String, Object> finalData = new HashMap<>();
        finalData.put("gradleVersion", new TemplateGradleVersion()); // TODO GradleVersion should be injected somewhere else
        finalData.putAll(data);
        paramsTemplate.process(finalData, writer, null);
        Properties props = new Properties();
        props.load(new StringReader(writer.toString()));
        for (Object key : props.keySet()) {
            finalData.put((String) key, props.get(key));
        }
        paramsTemplateFile.delete();
        return finalData;
    }

    @Override
    public void processTemplate(File templateRepoCloneLocation, File file, File targetFile, Map<String, Object> finalData) throws Exception {
        URI baseUri = templateRepoCloneLocation.toURI();
        URI templateUri = file.toURI();
        String templateRelativePath = baseUri.relativize(templateUri).getPath();
        Template template = configuration.getTemplate(templateRelativePath);
        targetFile.getParentFile().mkdirs();
        FileUtils.touch(targetFile);
        Writer out = new OutputStreamWriter(new FileOutputStream(targetFile));
        template.process(finalData, out, null);
    }
}
