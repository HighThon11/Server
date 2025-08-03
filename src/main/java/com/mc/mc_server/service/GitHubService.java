package com.mc.mc_server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.mc_server.dto.GitHubCommit;
import com.mc.mc_server.dto.GitHubCommitDetail;
import com.mc.mc_server.dto.GitHubFile;
import com.mc.mc_server.dto.GitHubRepository;
import com.mc.mc_server.dto.GitHubUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {
    
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + token);
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }
    
    /**
     * GitHub 토큰으로 사용자명(username)만 조회합니다.
     */
    public String getUsername(String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GITHUB_API_BASE_URL + "/user",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("login").asText();
        } catch (Exception e) {
            throw new RuntimeException("GitHub 사용자명을 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * GitHub 토큰으로 사용자 정보를 조회합니다.
     */
    public GitHubUser getUserInfo(String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GITHUB_API_BASE_URL + "/user",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            GitHubUser user = new GitHubUser();
            
            user.setId(jsonNode.get("id").asLong());
            user.setLogin(jsonNode.get("login").asText());
            
            // null 체크가 필요한 필드들
            if (jsonNode.has("name") && !jsonNode.get("name").isNull()) {
                user.setName(jsonNode.get("name").asText());
            }
            if (jsonNode.has("email") && !jsonNode.get("email").isNull()) {
                user.setEmail(jsonNode.get("email").asText());
            }
            if (jsonNode.has("avatar_url") && !jsonNode.get("avatar_url").isNull()) {
                user.setAvatarUrl(jsonNode.get("avatar_url").asText());
            }
            if (jsonNode.has("bio") && !jsonNode.get("bio").isNull()) {
                user.setBio(jsonNode.get("bio").asText());
            }
            if (jsonNode.has("company") && !jsonNode.get("company").isNull()) {
                user.setCompany(jsonNode.get("company").asText());
            }
            if (jsonNode.has("location") && !jsonNode.get("location").isNull()) {
                user.setLocation(jsonNode.get("location").asText());
            }
            if (jsonNode.has("blog") && !jsonNode.get("blog").isNull()) {
                user.setBlog(jsonNode.get("blog").asText());
            }
            if (jsonNode.has("public_repos")) {
                user.setPublicRepos(jsonNode.get("public_repos").asInt());
            }
            if (jsonNode.has("public_gists")) {
                user.setPublicGists(jsonNode.get("public_gists").asInt());
            }
            if (jsonNode.has("followers")) {
                user.setFollowers(jsonNode.get("followers").asInt());
            }
            if (jsonNode.has("following")) {
                user.setFollowing(jsonNode.get("following").asInt());
            }
            if (jsonNode.has("created_at")) {
                user.setCreatedAt(jsonNode.get("created_at").asText());
            }
            if (jsonNode.has("updated_at")) {
                user.setUpdatedAt(jsonNode.get("updated_at").asText());
            }
            
            return user;
        } catch (Exception e) {
            throw new RuntimeException("GitHub 사용자 정보를 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    public List<GitHubRepository> getUserRepositories(String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GITHUB_API_BASE_URL + "/user/repos?per_page=100&sort=updated",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<GitHubRepository> repositories = new ArrayList<>();
            
            for (JsonNode repoNode : jsonNode) {
                GitHubRepository repo = new GitHubRepository();
                repo.setId(repoNode.get("id").asLong());
                repo.setName(repoNode.get("name").asText());
                repo.setFullName(repoNode.get("full_name").asText());
                repo.setDescription(repoNode.has("description") && !repoNode.get("description").isNull() 
                    ? repoNode.get("description").asText() : null);
                repo.setHtmlUrl(repoNode.get("html_url").asText());
                repo.setDefaultBranch(repoNode.get("default_branch").asText());
                repo.setPrivateRepo(repoNode.get("private").asBoolean());
                repo.setCreatedAt(repoNode.get("created_at").asText());
                repo.setUpdatedAt(repoNode.get("updated_at").asText());
                
                repositories.add(repo);
            }
            
            return repositories;
        } catch (Exception e) {
            throw new RuntimeException("레포지토리 목록을 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    public List<GitHubCommit> getRepositoryCommits(String token, String owner, String repo, int page, int perPage) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = String.format("%s/repos/%s/%s/commits?page=%d&per_page=%d", 
                GITHUB_API_BASE_URL, owner, repo, page, perPage);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<GitHubCommit> commits = new ArrayList<>();
            
            for (JsonNode commitNode : jsonNode) {
                GitHubCommit commit = new GitHubCommit();
                commit.setSha(commitNode.get("sha").asText());
                
                JsonNode commitInfo = commitNode.get("commit");
                commit.setMessage(commitInfo.get("message").asText());
                
                JsonNode author = commitInfo.get("author");
                commit.setAuthorName(author.get("name").asText());
                commit.setAuthorEmail(author.get("email").asText());
                commit.setDate(author.get("date").asText());
                
                commit.setHtmlUrl(commitNode.get("html_url").asText());
                
                commits.add(commit);
            }
            
            return commits;
        } catch (Exception e) {
            throw new RuntimeException("커밋 목록을 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    public GitHubCommitDetail getCommitDetail(String token, String owner, String repo, String sha) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = String.format("%s/repos/%s/%s/commits/%s", 
                GITHUB_API_BASE_URL, owner, repo, sha);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            GitHubCommitDetail commitDetail = new GitHubCommitDetail();
            
            commitDetail.setSha(jsonNode.get("sha").asText());
            
            JsonNode commitInfo = jsonNode.get("commit");
            commitDetail.setMessage(commitInfo.get("message").asText());
            
            JsonNode author = commitInfo.get("author");
            commitDetail.setAuthorName(author.get("name").asText());
            commitDetail.setAuthorEmail(author.get("email").asText());
            commitDetail.setDate(author.get("date").asText());
            
            JsonNode stats = jsonNode.get("stats");
            commitDetail.setAdditions(stats.get("additions").asInt());
            commitDetail.setDeletions(stats.get("deletions").asInt());
            commitDetail.setTotal(stats.get("total").asInt());
            
            // 파일 변경 내용
            List<GitHubFile> files = new ArrayList<>();
            JsonNode filesNode = jsonNode.get("files");
            for (JsonNode fileNode : filesNode) {
                GitHubFile file = new GitHubFile();
                file.setFilename(fileNode.get("filename").asText());
                file.setStatus(fileNode.get("status").asText());
                file.setAdditions(fileNode.get("additions").asInt());
                file.setDeletions(fileNode.get("deletions").asInt());
                file.setChanges(fileNode.get("changes").asInt());
                
                if (fileNode.has("patch") && !fileNode.get("patch").isNull()) {
                    file.setPatch(fileNode.get("patch").asText());
                }
                
                files.add(file);
            }
            commitDetail.setFiles(files);
            
            return commitDetail;
        } catch (Exception e) {
            throw new RuntimeException("커밋 상세 정보를 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 파일의 현재 내용을 가져옵니다.
     */
    public String getFileContent(String token, String owner, String repo, String path, String branch) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = String.format("%s/repos/%s/%s/contents/%s?ref=%s", 
                GITHUB_API_BASE_URL, owner, repo, path, branch);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String encodedContent = jsonNode.get("content").asText();
            
            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(encodedContent.replaceAll("\\s", ""));
            return new String(decodedBytes);
        } catch (Exception e) {
            throw new RuntimeException("파일 내용을 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 파일의 SHA를 가져옵니다 (업데이트를 위해 필요).
     */
    public String getFileSha(String token, String owner, String repo, String path, String branch) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = String.format("%s/repos/%s/%s/contents/%s?ref=%s", 
                GITHUB_API_BASE_URL, owner, repo, path, branch);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("sha").asText();
        } catch (Exception e) {
            throw new RuntimeException("파일 SHA를 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * GitHub에 파일을 업데이트합니다.
     */
    public void updateFile(String token, String owner, String repo, String path, String content, 
                          String commitMessage, String branch, String sha) {
        try {
            HttpHeaders headers = createHeaders(token);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", commitMessage);
            requestBody.put("content", Base64.getEncoder().encodeToString(content.getBytes()));
            requestBody.put("branch", branch);
            requestBody.put("sha", sha);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = String.format("%s/repos/%s/%s/contents/%s", 
                GITHUB_API_BASE_URL, owner, repo, path);
            
            restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("파일 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 여러 파일을 한 번에 커밋합니다 (Tree API 사용).
     */
    public String createCommitWithMultipleFiles(String token, String owner, String repo, 
                                               Map<String, String> files, String commitMessage, String branch) {
        try {
            // 1. 현재 브랜치의 최신 커밋 SHA 가져오기
            String latestCommitSha = getLatestCommitSha(token, owner, repo, branch);
            
            // 2. Tree 생성
            String treeSha = createTree(token, owner, repo, files, latestCommitSha);
            
            // 3. 커밋 생성
            String commitSha = createCommit(token, owner, repo, commitMessage, treeSha, latestCommitSha);
            
            // 4. 브랜치 업데이트
            updateBranchReference(token, owner, repo, branch, commitSha);
            
            return commitSha;
        } catch (Exception e) {
            throw new RuntimeException("다중 파일 커밋에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String getLatestCommitSha(String token, String owner, String repo, String branch) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = String.format("%s/repos/%s/%s/git/refs/heads/%s", 
                GITHUB_API_BASE_URL, owner, repo, branch);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("object").get("sha").asText();
        } catch (Exception e) {
            throw new RuntimeException("최신 커밋 SHA를 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
    
    private String createTree(String token, String owner, String repo, 
                             Map<String, String> files, String baseTreeSha) {
        try {
            HttpHeaders headers = createHeaders(token);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("base_tree", baseTreeSha);
            
            List<Map<String, Object>> tree = new ArrayList<>();
            for (Map.Entry<String, String> file : files.entrySet()) {
                Map<String, Object> treeItem = new HashMap<>();
                treeItem.put("path", file.getKey());
                treeItem.put("mode", "100644");
                treeItem.put("type", "blob");
                treeItem.put("content", file.getValue());
                tree.add(treeItem);
            }
            requestBody.put("tree", tree);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = String.format("%s/repos/%s/%s/git/trees", 
                GITHUB_API_BASE_URL, owner, repo);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("sha").asText();
        } catch (Exception e) {
            throw new RuntimeException("Tree 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String createCommit(String token, String owner, String repo, 
                               String message, String treeSha, String parentSha) {
        try {
            HttpHeaders headers = createHeaders(token);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("tree", treeSha);
            requestBody.put("parents", List.of(parentSha));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = String.format("%s/repos/%s/%s/git/commits", 
                GITHUB_API_BASE_URL, owner, repo);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("sha").asText();
        } catch (Exception e) {
            throw new RuntimeException("커밋 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    private void updateBranchReference(String token, String owner, String repo, 
                                      String branch, String commitSha) {
        try {
            HttpHeaders headers = createHeaders(token);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sha", commitSha);
            requestBody.put("force", true);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = String.format("%s/repos/%s/%s/git/refs/heads/%s", 
                GITHUB_API_BASE_URL, owner, repo, branch);
            
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("브랜치 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
}
