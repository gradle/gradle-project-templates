package org.gradlex.archetypes;

import org.eclipse.jgit.api.Git;

import java.io.File;

public abstract class TemplateRepository {

    public static TemplateRepository from(String url) throws UnsupportedTemplateRepositoryTypeException  {
        if (url.startsWith("https://github")) {
            return new GithubTemplateRepository(url);
        } else {
            throw new UnsupportedTemplateRepositoryTypeException(url);
        }
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
