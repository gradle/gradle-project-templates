package org.gradlex.archetypes;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TemplateEngine {

    void initialize(File templateRepoCloneLocation) throws Exception; // TODO consider more specific exception here

    Map<String, Object>  processTemplateMetadata(File templateRepoCloneLocation, File file, List<String> metadata, Map<String, Object> data) throws Exception;

    void processTemplate(File localRepoDir, File file, File targetFile, Map<String, Object> data) throws Exception;
}
