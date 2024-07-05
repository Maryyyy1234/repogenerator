package com.example.repogenerator.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class GitUtil {

    public static void clone(String repoUrl, File repoDir) {
        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(repoDir)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public static void pull(File repoDir) {
        try (Git git = Git.open(repoDir)) {
            git.pull().call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void push(File repoDir, String remoteUrl) {
        try (Git git = Git.open(repoDir)) {
            git.push().setRemote(remoteUrl).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
