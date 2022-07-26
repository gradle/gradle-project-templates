package org.gradlex.archetypes;

import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.Map;

public interface TemplateEngine {

    void initialize(File templateRepoCloneLocation) throws Exception; // TODO consider more specific exception here

    void processTemplates(Logger logger, File targetLocation, File templateRepoCloneLocation,  Map<String, Object> data) throws Exception;
}
