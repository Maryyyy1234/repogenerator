package com.example.repogenerator.controller;

import com.example.repogenerator.service.BitbucketService;
import com.example.repogenerator.service.GitLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/gitlab/sync-repo/{repoName}")
    public void syncGitLabRepo(@PathVariable String repoName) {
        gitLabService.updateTargetRepo(repoName);
    }

    @PostMapping("/bitbucket/sync-all")
    public void syncAllBitbucketRepos() {
        bitbucketService.syncAllRepos();
    }

    @PostMapping("/gitlab/sync-all")
    public void syncAllGitLabRepos() {
        gitLabService.syncTargetRepos();
    }
}
