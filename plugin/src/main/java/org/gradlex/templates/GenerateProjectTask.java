package org.gradlex.templates;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.tasks.userinput.UserInputHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.buildinit.tasks.InitBuild;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class GenerateProjectTask extends InitBuild {

    private static final String templateOptionsFilePath = "templateOptions.json";
    private final Directory projectDir = getProject().getLayout().getProjectDirectory();

    @Input
    @Option(option = "template", description = "Specifies the template to use")
    @Optional
    public abstract Property<String> getTemplate();

    @Input
    @Option(option = "list", description = "Lists available templates")
    @Optional
    public abstract Property<Boolean> getList();

    @Override
    public void setupProjectLayout() {
        // hack superclass to do nothing here
    }
    @TaskAction

    public void taskAction() throws Exception {
        if (getList().isPresent()) {
            getLogger().quiet("Listing available templates");
        } else if (getTemplate().isPresent()) {
            materializeTemplate();
        } else {
            getLogger().error("Task requires either the --template or the --list task option");
        }
    }

    private void materializeTemplate() throws Exception {
        String url = getTemplate().get();

        // step 1: clone repository
        File localRepoDir = getProject().getLayout().getBuildDirectory().dir("tmp/gitClone").get().getAsFile();
        File targetDir = projectDir.getAsFile();
        getLogger().info("Cloning template repository. Source: " + url + ", destination: " + targetDir.getAbsolutePath() + ".");
        TemplateRepository.from(url).clone(localRepoDir);

        // step2: parse templateOptions and read user input
        File optionsFile = new File(localRepoDir, templateOptionsFilePath);
        Map<String, Object> data = loadTemplateData(optionsFile);

        // step3: generate files
        new TemplateGeneration().processTemplates(getProject().getLogger(), targetDir, localRepoDir, data, new FreemarkerTemplateEngine());

        // step4: delete cloned template repo
        FileUtils.deleteDirectory(localRepoDir);
    }

    private Map<String, Object> loadTemplateData(File optionsFile) throws IOException {
        if (!optionsFile.exists()) {
            getLogger().info("No template options file found at " + optionsFile);
            return new HashMap<>();
        } else {
            getLogger().info("Using template options file " + optionsFile);
            Descriptor descriptor = Descriptor.read(optionsFile);
            return new Interrogator(getServices().get(UserInputHandler.class)).askQuestions(descriptor.getQuestions());
        }
    }
}

