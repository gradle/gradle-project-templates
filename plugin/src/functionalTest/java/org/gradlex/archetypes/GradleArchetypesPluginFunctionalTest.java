/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.gradlex.archetypes;

import com.jcraft.jsch.IO;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleArchetypesPluginFunctionalTest {

    File projectDir;

    @BeforeEach
    void setup(TestInfo testInfo) throws IOException {
        projectDir = new File(System.getProperty("testRootDir"), testInfo.getTestClass().get().getName() + "." + testInfo.getTestMethod().get().getName());
        FileUtils.deleteDirectory(projectDir);
        projectDir.mkdirs();
    }

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    @Test void canRunTask() throws IOException {
        String pluginRepo = System.getProperty("pluginRepo");

        String initGradleText = "\n" +
                "initscript {                                                              \n" +
                "    repositories {                                                        \n" +
                "        mavenCentral()                                                    \n" +
                "        maven {                                                           \n" +
                "             url ='" + pluginRepo + "'                                    \n" +
                "        }                                                                 \n" +
                "    }                                                                     \n" +
                "                                                                          \n" +
                "    dependencies {                                                        \n" +
                "       classpath 'org.gradlex:plugin:0.0.1'                               \n" +
                "       classpath 'org.freemarker:freemarker:2.3.31'                       \n" + // TODO dependencies should be packaged with the plugin
                "       classpath 'org.eclipse.jgit:org.eclipse.jgit:5.7.0.202003110725-r' \n" + // TODO dependencies should be packaged with the plugin
                "       classpath 'commons-io:commons-io:1.4'                              \n" + // TODO dependencies should be packaged with the plugin
                "       classpath 'com.fasterxml.jackson.core:jackson-core:2.13.3'         \n" + // TODO dependencies should be packaged with the plugin
                "       classpath 'com.fasterxml.jackson.core:jackson-annotations:2.13.3'  \n" + // TODO dependencies should be packaged with the plugin
                "       classpath 'com.fasterxml.jackson.core:jackson-databind:2.13.3'     \n" + // TODO dependencies should be packaged with the plugin
                "    }                                                                     \n" +
                "}                                                                         \n" +
                "                                                                          \n" +
                "rootProject {                                                             \n" +
                "    apply plugin: org.gradlex.archetypes.GradleArchetypesPlugin           \n" +
                "}                                                                         \n" +
                "                                                                          \n";

        writeString(new File(projectDir, "init.gradle"), initGradleText);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        //runner.withPluginClasspath();
        runner.withArguments("init", "--init-script", "init.gradle", "--template", "https://github.com/donat/gradle-template-basic");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        // Verify the result
        assertTrue(result.getOutput().contains("Howdy"));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}