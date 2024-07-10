package com.example.repogenerator.controller;

import com.example.repogenerator.config.AppProperties;
import com.example.repogenerator.service.BitbucketService;
import com.example.repogenerator.service.GitLabService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RepoController {
    private final BitbucketService bitbucketService;
    private final GitLabService gitLabService;
    private final AppProperties properties;
    private  String localRepoPath;

    public RepoController(BitbucketService bitbucketService, GitLabService gitLabService, AppProperties properties) {
        this.bitbucketService = bitbucketService;
        this.gitLabService = gitLabService;
        this.properties = properties;
        localRepoPath = properties.getLocalRepoPath();
    }

    @Operation(summary = "Get Bitbucket Repositories")
    @CrossOrigin(origins = "*")
    @GetMapping("/bitbucket/repos")
    public ResponseEntity<List<RepoDto>> getAllBitbucketRepos() {
        List<String> bitbucketRepos = bitbucketService.getRepositories();
        List<RepoDto> repoDtos = bitbucketRepos.stream()
                .map(repo -> new RepoDto(repo, "bitbucket"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(repoDtos);
    }

    @Operation(summary = "Get GitLab Repositories")
    @GetMapping("/gitlab/repos")
    public List<String> getGitLabRepos() {
        return gitLabService.getRepositories();
    }

    @Operation(summary = "Update Local Bitbucket Repository")
    @CrossOrigin(origins = "*")
    @PostMapping("/bitbucket/update-local/{repoName}")
    public void updateBitbucketLocalRepo(@PathVariable String repoName) {
        bitbucketService.updateLocalRepo(repoName);
    }

    @Operation(summary = "Sync GitLab Repository")
    @PostMapping("/gitlab/sync-repo/{repoName}")
    public void syncGitLabRepo(@PathVariable String repoName) {
        gitLabService.updateTargetRepo(repoName);
    }

    @Operation(summary = "Sync All Bitbucket Repositories")
    @PostMapping("/bitbucket/sync-all")
    public void syncAllBitbucketRepos() {
        bitbucketService.syncAllRepos();
    }

    @Operation(summary = "Sync All GitLab Repositories")
    @PostMapping("/gitlab/sync-all")
    public void syncAllGitLabRepos() {
        gitLabService.syncTargetRepos();
    }

    @GetMapping("/local/repos")
    public ResponseEntity<List<RepoDto>> getAllLocalRepos() {
        List<String> localRepos = new ArrayList<>();
        File[] files = new File(localRepoPath).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    localRepos.add(file.getName());
                }
            }
        }
        List<RepoDto> repoDtos = localRepos.stream()
                .map(repo -> new RepoDto(repo, "local"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(repoDtos);
    }

}
