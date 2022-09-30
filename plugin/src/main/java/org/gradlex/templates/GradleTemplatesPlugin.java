package org.gradlex.templates;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradleTemplatesPlugin implements Plugin<Project> { // TODO rename gradleTemplatePlugin

    @Override
    public void apply(Project project) {
        if (project.getParent() == null) {
            project.afterEvaluate(project1 -> project1.getTasks().replace("init", GenerateProjectTask.class));
        }
    }


}
