package com.example.repogenerator.service;

import java.util.List;

public interface GitService {
    List<String> getRepositories();
    void updateLocalRepo(String repoName);
    void syncAllRepos();
    void updateTargetRepo(String repoName);
    void syncTargetRepos();
}
