package com.example.repogenerator.service;

import com.example.repogenerator.model.GitSource;

public interface GitLabService extends GitService {
    public void updateTargetRepo(String repoName, GitSource source);
    void syncTargetRepos();
}
