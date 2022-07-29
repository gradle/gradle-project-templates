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
            new MaterializeTemplateAction(
                    getServices(),
                    getTemplate().get(),
                    getProject().getLayout().getBuildDirectory().dir("tmp/gitClone").get().getAsFile(),
                    projectDir.getAsFile(), getLogger()
            ).execute();
        } else {
            getLogger().error("Task requires either the --template or the --list task option");
        }
    }
}

