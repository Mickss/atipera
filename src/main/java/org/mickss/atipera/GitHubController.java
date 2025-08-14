package org.mickss.atipera;

import org.mickss.atipera.dto.BranchDTO;
import org.mickss.atipera.dto.RepositoryDTO;
import org.mickss.atipera.github.GitHubBranchResponse;
import org.mickss.atipera.github.GitHubRepoResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final RestClient restClient;

    public GitHubController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping("/repos")
    public List<RepositoryDTO> getRepositories(@RequestParam String username) {
        String url = "https://api.github.com/users/%s/repos".formatted(username);

        GitHubRepoResponse[] reposResponse = restClient.get()
                .uri(url)
                .retrieve()
                .body(GitHubRepoResponse[].class);

        return Arrays.stream(reposResponse)
                .filter(repo -> !repo.isFork())
                .map(this::mapToRepositoryDTO)
                .toList();
    }

    private RepositoryDTO mapToRepositoryDTO(GitHubRepoResponse repo) {
        String branchesUrl = "https://api.github.com/repos/%s/%s/branches"
                .formatted(repo.getOwner().getLogin(), repo.getName());

        GitHubBranchResponse[] branchResponses = restClient.get()
                .uri(branchesUrl)
                .retrieve()
                .body(GitHubBranchResponse[].class);

        List<BranchDTO> branches = Arrays.stream(branchResponses)
                .map(branch -> new BranchDTO(branch.getName(), branch.getCommit().getSha()))
                .toList();

        return new RepositoryDTO(repo.getName(), repo.getOwner().getLogin(), branches);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public Map<String, String> handleHttpClientErrorException(HttpClientErrorException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("status", String.valueOf(ex.getStatusCode().value()));
        errorDetails.put("message", ex.getMessage());
        return errorDetails;
    }
}
