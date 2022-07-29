package org.gradlex.templates;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TemplateEngine {

    /**
     * @returns The begin tag of the template metadata. If file starts with the begin tag then the template generation
     * strips reads the file until the end tag is found, calls {@link #processTemplateMetadata(File, File, List, Map)}
     * with the content between the tags, and finally deletes the metadata from the file.
     */
    String getMetadataBeginTag();

    /**
     * @returns the end tag of the template metadata.
     * @see #getMetadataBeginTag()
     */
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
