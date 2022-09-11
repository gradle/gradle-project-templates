package org.gradlex.templates;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreemarkerTemplateEngine implements TemplateEngine {

    private Configuration configuration;

    @Override
    public String getMetadataBeginTag() {
        return "<#GradleTemplate>";
    }

    @Override
    public String getMetadataEndTag() {
        return "</#GradleTemplate>";
    }

    @Override
    public void initialize(File cloneDir) throws Exception {
        this.configuration = loadFreemarkerConfiguration(cloneDir);
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
    public String processTemplateMetadata(File cloneDir, File templateFile, List<String> metadata, Map<String, Object> data) throws Exception {
        String paramTemplateName = templateFile.getName() + ".GradleTemplate"; // TODO use relative path to avoid template caching
        File paramsTemplateFile = new File(cloneDir, paramTemplateName); // TODO check for collisions
        if (paramsTemplateFile.exists()) {
            paramsTemplateFile.delete();
            FileUtils.touch(paramsTemplateFile);
        }
        FileUtils.writeLines(paramsTemplateFile, metadata);

        Template paramsTemplate = configuration.getTemplate(paramTemplateName);
        StringWriter writer = new StringWriter();

        Map<String, Object> finalData = new HashMap<>();
        finalData.putAll(data);
        paramsTemplate.process(finalData, writer, null);
        return writer.toString();
    }

    @Override
    public void processTemplate(File cloneDir, File templateFile, File target, Map<String, Object> finalData) throws Exception {
        URI baseUri = cloneDir.toURI();
        URI templateUri = templateFile.toURI();
        String templateRelativePath = baseUri.relativize(templateUri).getPath();
        Template template = configuration.getTemplate(templateRelativePath);
        target.getParentFile().mkdirs();
        FileUtils.touch(target);
        Writer out = new OutputStreamWriter(new FileOutputStream(target));
        template.process(finalData, out, null);
    }
}
