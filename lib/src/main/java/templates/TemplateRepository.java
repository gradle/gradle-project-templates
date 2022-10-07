package templates;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TemplateRepository {
    private static String INDEX_FILE_URL = "https://raw.githubusercontent.com/gradle/gradle-project-templates/main/templateIndex.json";
    private static final Pattern INDEX_PATTERN = Pattern.compile("(.*)/(.*)");

    private static List<IndexItem> indexItems;

    public static TemplateRepository from(String url, TextFileDownloader downloader) throws Exception  {
        File source = new File(url);
        if (source.exists() && source.isDirectory()) {
            return new LocalTemplateRepository(source);
        } else if (url.startsWith("https://github")) {
            return new GithubTemplateRepository(url);
        } else {
            Matcher matcher = INDEX_PATTERN.matcher(url);
            if (matcher.matches()) {
                List<IndexItem> items = readIndexItems(downloader);
                String group = matcher.group(1);
                String template = matcher.group(2);
                for (IndexItem item : items) {
                    if (item.getGroup().equals(group) && item.getTemplate().equals(template)) {
                        return new GithubTemplateRepository(item.getUrl());
                    }
                }
            }
            throw new UnsupportedTemplateRepositoryTypeException(url);
        }
    }

    private static List<IndexItem>  readIndexItems(TextFileDownloader downloader) throws  Exception {
        if (indexItems == null) {
            String index = downloader.download(INDEX_FILE_URL);
            indexItems = parse(index);
        }
        return indexItems;
    }

    private static List<IndexItem> parse(String index) throws Exception {
        JsonMapper mapper = new JsonMapper();
        return Arrays.asList(mapper.readValue(index, IndexItem[].class));
    }

    public abstract void clone(File destination) throws TemplateRepositoryCloneException;

    private static class GithubTemplateRepository extends TemplateRepository {

        private final String url;

        GithubTemplateRepository(String url) {
            this.url = url;
        }

        @Override
        public void clone(File destination) throws TemplateRepositoryCloneException {
            try {
                Git.cloneRepository().setURI(url).setDirectory(destination).call();
            } catch (Exception e) {
                throw new TemplateRepositoryCloneException(url, e);
            }
        }
    }

    private static class LocalTemplateRepository extends TemplateRepository {

        private final File location;

        LocalTemplateRepository(File location) {
            this.location = location;
        }

        @Override
        public void clone(File destination) throws TemplateRepositoryCloneException {
            try {
                FileUtils.copyDirectory(location, destination);
            } catch (IOException e) {
                throw new TemplateRepositoryCloneException(location.getAbsolutePath(), e);
            }
        }
    }

    public static class TemplateRepositoryCloneException extends Exception {
        TemplateRepositoryCloneException(String url, Exception cause) {
            super("Exception thrown when cloning " + url, cause);
        }
    }

    public static class UnsupportedTemplateRepositoryTypeException extends Exception {
        UnsupportedTemplateRepositoryTypeException(String url) {
            super("Unsupported template repository: " + url);
        }
    }
}
