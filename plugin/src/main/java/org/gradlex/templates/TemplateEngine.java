package org.gradlex.templates;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TemplateEngine {

    /**
     * Called once before processing all files in the template repository.
     *
     * @param templateRepoCloneLocation TODO
     * @throws Exception TODO
     */
    void initialize(File templateRepoCloneLocation) throws Exception; // TODO consider more specific exception here

    Map<String, Object> processTemplateMetadata(File templateRepoCloneLocation, File file, List<String> metadata, Map<String, Object> data) throws Exception;

    void processTemplate(File localRepoDir, File file, File targetFile, Map<String, Object> data) throws Exception;
}
