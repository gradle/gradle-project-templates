package templates;

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

        // TODO the plugin variant generates Gradle wrapper files here; the standalone version should behave the same way
    }
}
