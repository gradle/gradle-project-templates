package org.gradlex.templates;

import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TemplateGeneration {

    public void processTemplates(Logger logger, File targetLocation, File templateRepoCloneLocation, Map<String, Object> data, TemplateEngine templateEngine) throws Exception {
        templateEngine.initialize(templateRepoCloneLocation);

        for (Object f : FileUtils.listFiles(templateRepoCloneLocation, null, true)) {
            File file = (File) f;
            if (file.isFile()) {
                URI fileUri = file.toURI();
                URI baseUri = templateRepoCloneLocation.toURI();
                String relativePath = baseUri.relativize(fileUri).getPath();
                logger.info("Processing " + relativePath);
                if (!isIgnored(relativePath)) {
                    processTemplate(logger, targetLocation, data, file, baseUri, templateRepoCloneLocation, templateEngine);
                }
            }
        }
    }

    private void processTemplate(Logger logger, File targetDir, Map<String, Object> data, File file, URI baseUri, File localRepoDir, TemplateEngine templateEngine) throws Exception {
        Template template = Template.from(file);
        Map<String, Object> finalData = new HashMap<>();
        if (template.hasMetadata()) {
            String metadataPropertiesString = templateEngine.processTemplateMetadata(localRepoDir, file, template.getMetadata(), data);
            Properties props = new Properties();
            props.load(new StringReader(metadataPropertiesString));
            for (Object key : props.keySet()) {
                finalData.put((String) key, props.get(key));
            }
            finalData.put("gradleVersion", new TemplateGradleVersion());
            template.deleteMetadata();
        }

        finalData.putAll(data);
        Object skip = finalData.get("skip");
        if (skip == null || !"true".equals(skip)) {
            String fileName = (String) finalData.get("file");
            String targetFileName = file.getName();
            URI generatedFileUri = new File(file.getParentFile(), targetFileName).toURI();
            String generatedFileRelativePath =  fileName == null ? baseUri.relativize(generatedFileUri).getPath() : fileName;
            File targetFile = new File(targetDir, generatedFileRelativePath);

            logger.info("Processing " + targetFile.getAbsolutePath());
            templateEngine.processTemplate(localRepoDir, file, targetFile, finalData);
        } else {
            logger.info("Skipping " + file.getAbsolutePath());
        }
    }

    private static boolean isIgnored(String relativePath) {
        return relativePath.startsWith(".git") || relativePath.startsWith(".gradle") || Arrays.asList("gradlew", "gradlew.bat").contains(relativePath) || relativePath.equals("templateOptions.json");
    }

    private static class Template {

        private final File file;
        private final List<String> lines;
        private final boolean hasMetadata;
        private final int endLine;
        private boolean metadataDeleted = false;

        private Template(File file, List<String> lines, boolean hasMetadata, int endLine) {
            this.file = file;
            this.lines = lines;
            this.hasMetadata = hasMetadata;
            this.endLine = endLine;
        }

        public boolean hasMetadata() {
            return hasMetadata;
        }

        public List<String> getMetadata() {
            return lines.subList(1, endLine);
        }

        static Template from(File file) throws IOException {
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

            return new Template(file, lines, hasMetadata, endLine);
        }

        public void deleteMetadata() throws IOException {
            if (!metadataDeleted) {
                FileUtils.writeLines(file, lines.subList(endLine + 1, lines.size()));
                metadataDeleted = true;
            } else {
                throw new RuntimeException("Metadata has been deleted from " + file.getAbsolutePath());
            }
        }
    }
}
