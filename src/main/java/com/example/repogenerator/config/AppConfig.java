package com.example.repogenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan
@Component
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

//    @Value("${bitbucket.bitbucketToken}")
    private String bitbucketToken;

//    @Value("${bitbucket.bitbucketUsername}")
    private String bitbucketUsername;

//    @Value("${gitlab.gitlabToken}")
    private String gitlabToken;

//    @Value("${gitlab.gitlabUsername}")
    private String gitlabUsername;

//    @Value("${local.localRepoPath}")
    private String localRepoPath;

    public String getBitbucketToken() {
        return bitbucketToken;
    }

    public void setBitbucketToken(String bitbucketToken) {
        this.bitbucketToken = bitbucketToken;
    }

    public String getBitbucketUsername() {
        return bitbucketUsername;
    }

    public void setBitbucketUsername(String bitbucketUsername) {
        this.bitbucketUsername = bitbucketUsername;
    }

    public String getGitlabToken() {
        return gitlabToken;
    }

    public void setGitlabToken(String gitlabToken) {
        this.gitlabToken = gitlabToken;
    }

    public String getGitlabUsername() {
        return gitlabUsername;
    }

    public void setGitlabUsername(String gitlabUsername) {
        this.gitlabUsername = gitlabUsername;
    }

    public String getLocalRepoPath() {
        return localRepoPath;
    }

    public void setLocalRepoPath(String localRepoPath) {
        this.localRepoPath = localRepoPath;
    }

    public GitlabConfig getGitlab() {
        return new GitlabConfig(gitlabToken, gitlabUsername);
    }

    public BitbucketConfig getBitbucket() {
        return new BitbucketConfig(bitbucketToken, bitbucketUsername);
    }

    public static class BitbucketConfig {
        private final String token;
        private final String username;

        public BitbucketConfig(String token, String username) {
            this.token = token;
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class GitlabConfig {
        private final String token;
        private final String username;

        public GitlabConfig(String token, String username) {
            this.token = token;
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }
    }
}
