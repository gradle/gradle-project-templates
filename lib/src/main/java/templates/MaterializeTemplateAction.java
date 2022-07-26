package templates;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MaterializeTemplateAction {

    private static final String TEMPLATE_OPTIONS_FILE_PATH = "templateOptions.json";

    private final InputHandler userInputHandler;
    private final String url;
    private final File cloneDir;
    private final File targetDir;
    private final TemplateLogger logger;

    public MaterializeTemplateAction(InputHandler inputHandler, String url, File cloneDir, File targetDir, TemplateLogger logger) {
        this.userInputHandler = inputHandler;
        this.url = url;
        this.cloneDir = cloneDir;
        this.targetDir = targetDir;
        this.logger = logger;
    }

    public void execute() throws Exception {
        // step 1: clone repository
        logger.info("Cloning template repository. Source: " + url + ", destination: " + cloneDir.getAbsolutePath() + ".");
        TemplateRepository.from(url, new TextFileDownloader()).clone(cloneDir);

        // step2: parse templateOptions and read user input
        File optionsFile = new File(cloneDir, TEMPLATE_OPTIONS_FILE_PATH);
        Map<String, Object> data = loadTemplateData(optionsFile);
        //data.put("gradleVersion", new TemplateGradleVersion()); // TODO how to set Gradle version in templates

        // step3: generate files
        new TemplateGeneration().processTemplates(cloneDir, targetDir, data, new FreemarkerTemplateEngine(), logger);

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
            return new Interrogator(userInputHandler).askQuestions(descriptor.getQuestions());
        }
    }
}
