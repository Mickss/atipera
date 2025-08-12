package org.mickss.atipera;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/repos")
    public ResponseEntity<List<JsonNode>> getRepositories(@RequestParam String username) throws JsonProcessingException {
        String url = "https://api.github.com/users/%s/repos".formatted(username);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        JsonNode reposRoot = objectMapper.readTree(response.getBody());

        List<JsonNode> repos = stream(reposRoot.spliterator(), false)
                .filter(repoNode -> !repoNode.get("fork").asBoolean())
                .map(this::mapJsonNode)
                .toList();
        return ResponseEntity.ok(repos);
    }

    private JsonNode mapJsonNode(JsonNode repoNode) {
        String repoName = repoNode.get("name").asText();
        String login = repoNode.get("owner").get("login").asText();
        String branchesUrl = "https://api.github.com/repos/%s/%s/branches".formatted(login, repoName);
        ResponseEntity<String> branchesResponse = restTemplate.getForEntity(branchesUrl, String.class);

        JsonNode branchesRoot;
        try {
            branchesRoot = objectMapper.readTree(branchesResponse.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<ObjectNode> branches = stream(branchesRoot.spliterator(), false)
                .map(branchNode -> objectMapper.createObjectNode()
                        .put("name", branchNode.get("name").asText())
                        .put("lastCommitSha", branchNode.get("commit").get("sha").asText()))
                .toList();

        return objectMapper.createObjectNode()
                .put("repoName", repoName)
                .put("ownerLogin", login)
                .set("branches", objectMapper.valueToTree(branches));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpClientErrorException(HttpClientErrorException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("status", String.valueOf(ex.getStatusCode().value()));
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, ex.getStatusCode());
    }
}
