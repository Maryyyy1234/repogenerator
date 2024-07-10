package com.example.repogenerator.service;

import com.example.repogenerator.config.AppProperties;
import com.example.repogenerator.util.GitUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BitbucketServiceImpl implements BitbucketService {

    private static final Logger logger = LoggerFactory.getLogger(BitbucketServiceImpl.class);

    private final RestTemplate restTemplate;
    private final AppProperties properties;
    private final GitUtil gitUtil;

    private HttpHeaders createHeaders(String username, String token) {

        String auth = username + ":" + token;

        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }

    private HttpHeaders createHeaders() {
        return createHeaders(properties.getBitbucketUsername(), properties.getBitbucketToken());
    }

    public List<String> getRepositories() {
        String url = "https://api.bitbucket.org/2.0/repositories/" + properties.getWorkSpace();
        logger.info("Fetching repositories from: {}", url);
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<BitbucketRepoResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, BitbucketRepoResponse.class);
        if (response.getBody() != null && response.getBody().getValues() != null) {
            logger.info("Repositories fetched successfully.");
            return Stream.of(response.getBody().getValues())
                    .map(BitbucketRepo::getName)
                    .collect(Collectors.toList());
        }

        logger.warn("Response body or values are null.");
        return List.of();
    }

    public void updateLocalRepo(String repoName) {
        String repoUrl = "https://bitbucket.org/" + properties.getWorkSpace() + "/" + repoName + ".git";
        File repoDir = new File(properties.getLocalRepoPath(), repoName);

        if (repoDir.exists()) {
            logger.info("Updating existing repository: {}", repoName);
            try (Git git = Git.open(repoDir)) {
                git.checkout().setName("master").call();
                git.pull()
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(properties.getBitbucketUsername(), properties.getBitbucketToken()))
                        .call();

                gitUtil.updateBranches(repoDir,properties.getBitbucketUsername(),properties.getBitbucketToken());
            } catch (Exception e) {
                logger.error("Error updating repository", e);
            }
        } else {
            logger.info("Cloning new repository: {}", repoName);
            gitUtil.cloneRepository(repoUrl, repoDir,properties.getBitbucketUsername(),properties.getBitbucketToken());
        }
    }

    @Override
    public void syncAllRepos() {
        List<String> repos = getRepositories();
        for (String repo : repos) {
            updateLocalRepo(repo);
        }
    }

    @Data
    public static class BitbucketRepo {
        private String slug;
        private String name;
    }

    @Data
    public static class BitbucketRepoResponse {
        private BitbucketRepo[] values;
    }
}