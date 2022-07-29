package org.gradlex.templates;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TemplateEngine {

    String getMetadataBeginTag();

    String getMetadataEndTag();

    /**
     * Called once before processing all files in the template repository.
     */
    void initialize(File cloneDir) throws Exception; // TODO consider more specific exception here

    /**
     * Returns the string version of the metadata
     */
    String processTemplateMetadata(File cloneDir, File templateFile, List<String> metadata, Map<String, Object> data) throws Exception; // TODO templateFile should not be necessary here

    /**
     * Generates a templateFile from the template
     */
    void processTemplate(File cloneDir, File templateFile, File target, Map<String, Object> data) throws Exception;
}
