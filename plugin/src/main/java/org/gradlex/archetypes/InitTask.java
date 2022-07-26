package org.gradlex.archetypes;

import org.gradle.api.tasks.TaskAction;
import org.gradle.buildinit.tasks.InitBuild;

public abstract class InitTask extends InitBuild {

    @Override
    public void setupProjectLayout() {
        // do nothing
    }

    @TaskAction
    public void initTaskAction() {
        getLogger().lifecycle("Howdy2");
    }
}

