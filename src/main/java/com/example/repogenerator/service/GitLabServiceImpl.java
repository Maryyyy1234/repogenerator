package com.example.repogenerator.service;

import com.example.repogenerator.config.AppProperties;
import com.example.repogenerator.util.GitUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
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
        String repoUrl = "https://gitlab.com/" + appProperties.getGitlabUsername() + "/" + repoName + ".git";
        File repoDir = new File(appProperties.getLocalRepoPath(), repoName);
        if (!repoDir.exists()) {
            logger.warn("Local repository not found: {}", repoName);
            return;
        }

        try {
            if (isRepositoryExistsInGitLab(repoName)) {
                updateGitLabRepo(repoName);
            } else {
                createGitLabRepo(repoName);
            }
            GitUtil.updateBranches(repoDir, appProperties.getGitlabUsername(), appProperties.getGitlabToken());
            GitUtil.pushAllBranches(repoDir, repoUrl, appProperties.getGitlabUsername(), appProperties.getGitlabToken());
            logger.info("Successfully pushed all branches of repository to GitLab: {}", repoName);
        } catch (Exception e) {
            logger.error("Error updating or creating repository: {}", repoName, e);
        }
    }

    private boolean isRepositoryExistsInGitLab(String repoName) {
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
                updateGitLabRepo(repoName);
            } else {
                logger.error("Error creating repository: {}", repoName, ex);
            }
        } catch (Exception e) {
            logger.error("Error creating repository: {}", repoName, e);
        }
    }

    private void updateGitLabRepo(String repoName) {
        String projectId = getProjectId(repoName);
        if (projectId == null) {
            logger.warn("Project {} not found in GitLab", repoName);
            return;
        }

        String url = "https://gitlab.com/api/v4/projects/" + projectId;
        GitLabCreateRepoRequest request = new GitLabCreateRepoRequest(repoName, appProperties.getGitlabUsername());
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", appProperties.getGitlabToken());
        HttpEntity<GitLabCreateRepoRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            logger.info("Successfully updated repository in GitLab: {}", repoName);
        } catch (Exception e) {
            logger.error("Error updating repository: {}", repoName, e);
        }
    }

    private String getProjectId(String repoName) {
        String url = "https://gitlab.com/api/v4/projects";
        String encodedRepoName = encodeRepoName(repoName);
        String requestUrl = url + "?search=" + encodedRepoName;
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", appProperties.getGitlabToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitLabRepoResponse[]> response = restTemplate.exchange(requestUrl, HttpMethod.GET, entity, GitLabRepoResponse[].class);
            if (response.getBody() != null && response.getBody().length > 0) {
                return String.valueOf(response.getBody()[0].getId());
            } else {
                logger.warn("Project {} not found in GitLab", repoName);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching project ID from GitLab: {}", repoName, e);
            return null;
        }
    }

    private String encodeRepoName(String repoName) {
        return UriUtils.encodePath(repoName, StandardCharsets.UTF_8.toString());
    }

    @Data
    private static class GitLabCreateRepoRequest {
        private String name;
        private String path;
        private String visibility;

        public GitLabCreateRepoRequest(String name, String path) {
            this.name = name;
            this.path = path;
            this.visibility = "private"; // Ensure it's set to "private" explicitly
        }
    }

    @Data
    private static class GitLabRepoResponse {
        private long id;
        private String name;
    }
}
