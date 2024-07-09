package com.example.repogenerator.service;

public interface GitLabService extends GitService {
    void updateTargetRepo(String repoName);
    void syncTargetRepos();
}
