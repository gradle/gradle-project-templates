package templates;

public class StandaloneLogger implements TemplateLogger {
    @Override
    public void info(String message) {
        System.out.println(message);
    }
}
