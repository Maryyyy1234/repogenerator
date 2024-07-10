package com.example.repogenerator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Data
@Primary
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String workSpace;
    private String bitbucketToken;
    private String bitbucketUsername;
    private String gitlabToken;
    private String gitlabUsername;
    private String localRepoPath;
}