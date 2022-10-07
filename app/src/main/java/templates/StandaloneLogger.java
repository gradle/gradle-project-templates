package templates;

public class StandaloneLogger implements TemplateLogger {
    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String msg, Throwable t) {
        System.err.println(msg);
        t.printStackTrace();
    }
}
