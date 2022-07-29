package org.gradlex.templates;

import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.tasks.userinput.UserInputHandler;
import org.gradle.api.logging.Logger;
import org.gradle.internal.service.ServiceRegistry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MaterializeTemplateAction {

    private static final String templateOptionsFilePath = "templateOptions.json";

    private final ServiceRegistry services;
    private final String url;
    private final File cloneDir;
    private final File targetDir;
    private final Logger logger;

    public MaterializeTemplateAction(ServiceRegistry services, String url, File cloneDir, File targetDir, Logger logger) {
        this.services = services;
        this.url = url;
        this.cloneDir = cloneDir;
        this.targetDir = targetDir;
        this.logger = logger;
    }

    public void execute() throws Exception {
        // step 1: clone repository
        logger.info("Cloning template repository. Source: " + url + ", destination: " + targetDir.getAbsolutePath() + ".");
        TemplateRepository.from(url).clone(cloneDir);

        // step2: parse templateOptions and read user input
        File optionsFile = new File(cloneDir, templateOptionsFilePath);
        Map<String, Object> data = loadTemplateData(optionsFile);

        // step3: generate files
        new TemplateGeneration().processTemplates(logger, targetDir, cloneDir, data, new FreemarkerTemplateEngine());

        // step4: delete cloned template repo
        FileUtils.deleteDirectory(cloneDir);
    }

    private Map<String, Object> loadTemplateData(File optionsFile) throws IOException {
        if (!optionsFile.exists()) {
            logger.info("No template options file found at " + optionsFile);
            return new HashMap<>();
        } else {
            logger.info("Using template options file " + optionsFile);
            Descriptor descriptor = Descriptor.read(optionsFile);
            return new Interrogator(services.get(UserInputHandler.class)).askQuestions(descriptor.getQuestions());
        }
    }
}
