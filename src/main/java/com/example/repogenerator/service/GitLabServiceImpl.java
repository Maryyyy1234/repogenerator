package com.example.repogenerator.service;

import com.example.repogenerator.config.AppProperties;
import com.example.repogenerator.model.GitSource;
import com.example.repogenerator.util.GitUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitLabServiceImpl implements GitLabService {

    private static final Logger logger = LoggerFactory.getLogger(GitLabServiceImpl.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    @Autowired
    public GitLabServiceImpl(AppProperties appProperties, RestTemplate restTemplate) {
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<String> getRepositories() {
        String url = "https://gitlab.com/api/v4/projects?owned=true";
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", appProperties.getGitlabToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GitLabRepoResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, GitLabRepoResponse[].class);
        List<String> repoNames = new ArrayList<>();
        if (response.getBody() != null) {
            for (GitLabRepoResponse repo : response.getBody()) {
                repoNames.add(repo.getName());
            }
        }
        return repoNames;
    }


    @Override
    public void syncTargetRepos() {
        File localRepos = new File(appProperties.getLocalRepoPath());
        if (localRepos.exists() && localRepos.isDirectory()) {
            for (File repo : localRepos.listFiles()) {
                if (repo.isDirectory()) {
                    updateTargetRepo(repo.getName());
                }
            }
        } else {
            logger.warn("No local repositories found to sync");
        }
    }


    @Override
    public void updateTargetRepo(String repoName) {
        String repoUrl;
        GitSource source;

        if (isGitLabRepo(repoName)) {
            repoUrl = "https://gitlab.com/" + appProperties.getGitlabUsername() + "/" + repoName + ".git";
            source = GitSource.GITLAB;
        } else {
            repoUrl = "https://bitbucket.org/" + appProperties.getBitbucketUsername() + "/" + repoName + ".git";
            source = GitSource.BITBUCKET;
        }

        File repoDir = new File(appProperties.getLocalRepoPath(), repoName);

        if (!repoDir.exists()) {
            logger.warn("Local repository not found: {}", repoName);
            return;
        }

        try {
            if (isRepositoryExistsInGitLab(repoName, source)) {
                logger.warn("Repository {} already exists in {}. Updating instead.", repoName, source);
                GitUtil.pushAllBranches(repoDir, repoUrl, appProperties.getGitlabUsername(), appProperties.getGitlabToken());
                logger.info("Successfully pushed all branches of repository to {}: {}", source, repoName);
            } else {
                createGitLabRepo(repoName);
                GitUtil.pushAllBranches(repoDir, repoUrl, appProperties.getGitlabUsername(), appProperties.getGitlabToken());
                logger.info("Successfully created and pushed all branches of repository to {}: {}", source, repoName);
            }
        } catch (Exception e) {
            logger.error("Error updating or creating repository: {}", repoName, e);
        }
    }

    private boolean isGitLabRepo(String repoName) {
        // Implement logic to determine if a repository belongs to GitLab
        return true; // Placeholder logic, replace with actual implementation
    }


    private boolean isRepositoryExistsInGitLab(String repoName, GitSource source) {
        List<String> repos = getRepositories();
        return repos.contains(repoName);
    }

    private void createGitLabRepo(String repoName) {
        String url = "https://gitlab.com/api/v4/projects";
        GitLabCreateRepoRequest request = new GitLabCreateRepoRequest(repoName, appProperties.getGitlabUsername());
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", appProperties.getGitlabToken());
        HttpEntity<GitLabCreateRepoRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.postForObject(url, entity, String.class);
            logger.info("Created new repository in GitLab: {}", repoName);
        } catch (HttpClientErrorException.BadRequest ex) {
            if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST) && ex.getResponseBodyAsString().contains("has already been taken")) {
                logger.warn("Repository {} already exists, updating instead.", repoName);
            } else {
                logger.error("Error creating repository: {}", repoName, ex);
            }
        } catch (Exception e) {
            logger.error("Error creating repository: {}", repoName, e);
        }
    }

    @Data
    private static class GitLabCreateRepoRequest {
        private String name;
        private String path;
        private String visibility;

        public GitLabCreateRepoRequest(String name, String path) {
            this.name = name;
            this.path = path;
            this.visibility = "private";
        }
    }

    @Data
    private static class GitLabRepoResponse {
        private long id;
        private String name;
    }
}
