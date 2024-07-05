package com.example.repogenerator.controller;

import com.example.repogenerator.service.BitbucketService;
import com.example.repogenerator.service.GitLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RepoController {

    @Autowired
    private BitbucketService bitbucketService;

    @Autowired
    private GitLabService gitLabService;

    @GetMapping("/bitbucket/repos")
    public List<String> getBitbucketRepos() {
        return bitbucketService.getRepositories();
    }


    @GetMapping("/gitlab/repos")
    public List<String> getGitLabRepos() {
        return gitLabService.getRepositories();
    }

    @PostMapping("/bitbucket/update-local/{repoName}")
    public void updateBitbucketLocalRepo(@PathVariable String repoName) {
        bitbucketService.updateLocalRepo(repoName);
    }

    @PostMapping("/gitlab/update-local/{repoName}")
    public void updateGitLabLocalRepo(@PathVariable String repoName) {
        gitLabService.updateLocalRepo(repoName);
    }

    @PostMapping("/bitbucket/update-target/{repoName}")
    public void updateBitbucketTargetRepo(@PathVariable String repoName) {
        bitbucketService.updateTargetRepo(repoName);
    }

    @PostMapping("/gitlab/update-target/{repoName}")
    public void updateGitLabTargetRepo(@PathVariable String repoName) {
        gitLabService.updateTargetRepo(repoName);
    }

    @PostMapping("/bitbucket/sync-all")
    public void syncAllBitbucketRepos() {
        bitbucketService.syncAllRepos();
    }

    @PostMapping("/gitlab/sync-all")
    public void syncAllGitLabRepos() {
        gitLabService.syncAllRepos();
    }

    @PostMapping("/bitbucket/sync-target")
    public void syncTargetBitbucketRepos() {
        bitbucketService.syncTargetRepos();
    }

    @PostMapping("/gitlab/sync-target")
    public void syncTargetGitLabRepos() {
        gitLabService.syncTargetRepos();
    }
}