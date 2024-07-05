package com.example.repogenerator.service;

import com.example.repogenerator.config.AppConfig;
import com.example.repogenerator.model.GitLabRepo;
import com.example.repogenerator.util.GitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitLabServiceImpl implements GitLabService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppConfig appConfig;

    @Override
    public List<String> getRepositories() {
        String url = "https://gitlab.com/api/v4/projects?owned=true";
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", appConfig.getGitlab().getToken());
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
    public void updateLocalRepo(String repoName) {
        String repoUrl = "https://gitlab.com/" + appConfig.getGitlab().getUsername() + "/" + repoName + ".git";
        File repoDir = new File("local/" + repoName);
        if (repoDir.exists()) {
            GitUtil.pull(repoDir);
        } else {
            GitUtil.clone(repoUrl, repoDir);
        }
    }

    @Override
    public void syncAllRepos() {
        List<String> repos = getRepositories();
        for (String repo : repos) {
            updateLocalRepo(repo);
        }
    }

    @Override
    public void updateTargetRepo(String repoName) {
        String repoUrl = "https://gitlab.com/" + appConfig.getGitlab().getUsername() + "/" + repoName + ".git";
        File repoDir = new File("local/" + repoName);
        if (!repoDir.exists()) {
            createGitLabRepo(repoName);
            GitUtil.clone(repoUrl, repoDir);
        }
        GitUtil.push(repoDir, repoUrl);
    }

    @Override
    public void syncTargetRepos() {
        File localRepos = new File("local");
        for (File repo : localRepos.listFiles()) {
            if (repo.isDirectory()) {
                updateTargetRepo(repo.getName());
            }
        }
    }

    private void createGitLabRepo(String repoName) {
        String url = "https://gitlab.com/api/v4/projects";
        GitLabCreateRepoRequest request = new GitLabCreateRepoRequest(repoName, appConfig.getGitlab().getUsername());
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", appConfig.getGitlab().getToken());
        HttpEntity<GitLabCreateRepoRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.postForObject(url, entity, String.class);
    }

    private static class GitLabRepoResponse {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class GitLabCreateRepoRequest {
        private String name;
        private String path;
        private boolean visibility = true;

        public GitLabCreateRepoRequest(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isVisibility() {
            return visibility;
        }

        public void setVisibility(boolean visibility) {
            this.visibility = visibility;
        }
    }
}