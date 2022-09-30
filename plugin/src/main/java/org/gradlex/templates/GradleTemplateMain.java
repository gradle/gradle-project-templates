package org.gradlex.templates;

import org.gradlex.templates.standalone.StandaloneLogger;
import org.gradlex.templates.standalone.StandaloneUserInputHandler;

import java.io.File;

public class GradleTemplateMain {
    public static void main(String[] args) {
        if (args.length == 2 && "--template".equals(args[0])) {
            String template = args[1];
            runStandaloneTemplateGeneration(template);
        } else {
            System.out.println("TODO print help");
            System.exit(1);
        }
    }

    private static void runStandaloneTemplateGeneration(String template) {
        try {
            new MaterializeTemplateAction(
                    new StandaloneUserInputHandler(),
                    template,
                    new File("build/tmp/gitClone"),
                    new File("."),
                    new StandaloneLogger()
            ).execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
