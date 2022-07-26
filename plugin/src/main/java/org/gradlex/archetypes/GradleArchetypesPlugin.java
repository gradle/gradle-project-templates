/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.gradlex.archetypes;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradleArchetypesPlugin implements Plugin<Project> {
    public void apply(Project project) {
        // Register a task
        project.getTasks().register("greeting", task -> {
            task.doLast(s -> System.out.println("Hello from plugin 'org.gradlex.archetypes.greeting'"));
        });

        if (project.getParent() == null) {
            project.afterEvaluate(project1 -> project1.getTasks().replace("init", InitTask.class));
        }
    }
}