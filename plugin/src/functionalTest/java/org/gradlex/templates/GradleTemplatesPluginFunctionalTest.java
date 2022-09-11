package org.gradlex.templates;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleTemplatesPluginFunctionalTest {

    // we use the build directory instead of @TmpDir to make it easier to manually inspect outcome
    File projectDir;
    File templateRepoDir;

    @BeforeEach
    void setup(TestInfo testInfo) throws IOException {
        // clean project directories
        String testId = testInfo.getTestClass().get().getName() + "." + testInfo.getTestMethod().get().getName();
        projectDir = new File(System.getProperty("testRootDir"), testId + "/project");
        templateRepoDir = new File(System.getProperty("testRootDir"), testId + "/templateRepo");
        FileUtils.deleteDirectory(projectDir);
        FileUtils.deleteDirectory(templateRepoDir);
        projectDir.mkdirs();
        templateRepoDir.mkdirs();

        // setup init script
        String pluginRepo = System.getProperty("pluginRepo");
        String initGradleText = "                                                          \n" +
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
                "    }                                                                     \n" +
                "}                                                                         \n" +
                "                                                                          \n" +
                "rootProject {                                                             \n" +
                "    apply plugin: org.gradlex.templates.GradleTemplatesPlugin             \n" +
                "}                                                                         \n" +
                "                                                                          \n";

        writeString(new File(projectDir, "init.gradle"), initGradleText);
    }

    @Test
    @DisplayName("Can list available templates")
    void canListAvailableTemplates() throws Exception {
        // when:
        BuildResult result = runBuild("init", "--list", "--init-script", "init.gradle");;

        // then:
        result.getOutput().contains("what");
    }

    @Test
    @DisplayName("Can generate project from template repository")
    void canGenerateProjectFromTemplateRepository() throws IOException {
        String settingsScriptTemplate = "<#GradleTemplate>\n" +
                "file=settings.gradle<#if dsl =='kotlin'>.kts</#if>\n" +
                "</#GradleTemplate>\n" +
                "\n" +
                "rootProject.name = \"${projectName}\"";
        String templateOptions = "{\n" +
                "    \"name\": \"basic\",\n" +
                "    \"questions\": [\n" +
                "        {\n" +
                "            \"type\": \"string\",\n" +
                "            \"name\": \"projectName\",\n" +
                "            \"question\": \"Project Name\",\n" +
                "            \"default\": \"example\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"choice\",\n" +
                "            \"name\": \"dsl\",\n" +
                "            \"question\": \"DSL\",\n" +
                "            \"choices\": {\n" +
                "                \"kotlin\": \"Kotlin\",\n" +
                "                \"groovy\": \"Groovy\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}⏎";
        writeString(new File(templateRepoDir, "settings.gradle"), settingsScriptTemplate);
        writeString(new File(templateRepoDir, "templateOptions.json"), templateOptions);

        // setup:


        // when:
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runBuild("init", "--init-script", "init.gradle", "--template", templateRepoDir.getAbsolutePath());
        runner.withProjectDir(projectDir);
        runner.build();

        // then:
        File settingsFile = new File(projectDir, "settings.gradle.kts");
        assertTrue(settingsFile.exists());
        assertEquals(FileUtils.readFileToString(settingsFile), "\nrootProject.name = \"example\"\n");
    }

    private BuildResult runBuild(String... args) {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withArguments(args);
        runner.withProjectDir(projectDir);
        return runner.build();
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
