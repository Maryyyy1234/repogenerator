package com.example.repogenerator.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitUtil {

    private static final Logger logger = LoggerFactory.getLogger(GitUtil.class);

    public static void cloneRepository(String repoUrl, File repoDir, String username, String token) {
        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();
            logger.info("Successfully cloned repository: {}", repoUrl);
        } catch (GitAPIException e) {
            logger.error("Error cloning repository: {}", repoUrl, e);
        }
    }
    public static void updateBranches(File repoDir, String username, String token) {
        try (Git git = Git.open(repoDir)) {
            git.fetch()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();
            logger.info("Successfully updated branches for repository: {}", repoDir.getName());
        } catch (Exception e) {
            logger.error("Error updating branches for repository: {}", repoDir.getName(), e);
        }
    }
    public static void pushAllBranches(File repoDir, String remoteUrl, String username, String token) {
        try (Git git = Git.open(repoDir)) {
            git.branchList().call().forEach(branchRef -> {
                try {
                    git.push()
                            .setRemote(remoteUrl)
                            .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                            .add(branchRef.getName())
                            .call();
                    logger.info("Successfully pushed branch: {} to remote: {}", branchRef.getName(), remoteUrl);
                } catch (GitAPIException e) {
                    logger.error("Error pushing branch: {} to remote: {}", branchRef.getName(), remoteUrl, e);
                }
            });
            logger.info("Successfully pushed all branches for repository: {}", repoDir.getName());
        } catch (Exception e) {
            logger.error("Error pushing all branches for repository: {}", repoDir.getName(), e);
        }
    }
}