package templates;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;

public class ListTemplatesAction {

    private static String INDEX_FILE_URL = "https://raw.githubusercontent.com/gradle/gradle-project-templates/main/templateIndex.json"; // TODO remode duplication
    private final TemplateLogger logger;
    private final TextFileDownloader textFileDownloader;

    public ListTemplatesAction(TemplateLogger logger, TextFileDownloader textFileDownloader) {
        this.logger = logger;
        this.textFileDownloader = textFileDownloader;
    }

    public void execute() throws Exception {
        logger.info("Downloading index file");
        String index = textFileDownloader.download(INDEX_FILE_URL);
        printIndex(parse(index));
    }

    private static List<IndexItem> parse(String index) throws Exception {
        JsonMapper mapper = new JsonMapper();
        return Arrays.asList(mapper.readValue(index, IndexItem[].class));
    }

    private void printIndex(List<IndexItem> items) {
        for (IndexItem item : items) {
            logger.info(item.getGroup() + "/" + item.getTemplate()  + " -> " + item.getUrl()); //TODO it was logger.quiet
        }
    }
}
