package com.example.repogenerator.service;

import com.example.repogenerator.config.AppConfig;
import com.example.repogenerator.util.GitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BitbucketServiceImpl implements BitbucketService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppConfig appConfig;

    @Override
    public List<String> getRepositories() {
        String url = "https://api.bitbucket.org/2.0/repositories/" + appConfig.getBitbucketUsername();
        System.out.println("Bitbucket Username: " + appConfig.getBitbucketUsername());
        BitbucketRepoResponse response = restTemplate.getForObject(url, BitbucketRepoResponse.class);
        List<String> repoNames = new ArrayList<>();
        if (response != null && response.getValues() != null) {
            for (BitbucketRepo repo : response.getValues()) {
                repoNames.add(repo.getSlug());
            }
        }
        return repoNames;
    }

    @Override
    public void updateLocalRepo(String repoName) {
        String repoUrl = "https://bitbucket.org/" + appConfig.getBitbucketUsername() + "/" + repoName + ".git";
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
        String repoUrl = "https://bitbucket.org/" + appConfig.getBitbucketUsername() + "/" + repoName + ".git";
        File repoDir = new File("local/" + repoName);
        if (!repoDir.exists()) {
            createBitbucketRepo(repoName);
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

    private void createBitbucketRepo(String repoName) {
        String url = "https://api.bitbucket.org/2.0/repositories/" + appConfig.getBitbucketUsername() + "/" + repoName;
        BitbucketCreateRepoRequest request = new BitbucketCreateRepoRequest(repoName);
        restTemplate.postForObject(url, request, String.class);
    }

    private static class BitbucketRepoResponse {
        private BitbucketRepo[] values;

        public BitbucketRepo[] getValues() {
            return values;
        }

        public void setValues(BitbucketRepo[] values) {
            this.values = values;
        }
    }

    private static class BitbucketRepo {
        private String slug;

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }
    }

    private static class BitbucketCreateRepoRequest {
        private String scm = "git";
        private String name;
        private boolean is_private = true;

        public BitbucketCreateRepoRequest(String name) {
            this.name = name;
        }

        public String getScm() {
            return scm;
        }

        public void setScm(String scm) {
            this.scm = scm;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isIs_private() {
            return is_private;
        }

        public void setIs_private(boolean is_private) {
            this.is_private = is_private;
        }
    }
}
