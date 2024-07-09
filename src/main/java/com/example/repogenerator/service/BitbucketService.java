package com.example.repogenerator.service;

import java.util.List;

public interface BitbucketService extends GitService{
    public void syncAllRepos();
    public void updateLocalRepo(String repoName);
}
