package org.mickss.atipera;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // mocking restTemplate because GitHub is limiting requests
    private RestTemplate restTemplate;

    @Test
    void shouldReturnRepositoriesForValidUser() throws Exception {

        // given
        ResponseEntity<String> repoResponse = ResponseEntity.status(HttpStatus.OK).body("""
                    [{
                        "name": "repo1",
                        "owner": { "login": "user1" },
                        "fork": false
                    }, {
                        "name": "repo2",
                        "owner": { "login": "user1" },
                        "fork": true
                    }, {
                        "name": "repo3",
                        "owner": { "login": "user1" },
                        "fork": false
                    }]
                """);
        Mockito.when(restTemplate.getForEntity("https://api.github.com/users/user1/repos", String.class)).thenReturn(repoResponse);

        ResponseEntity<String> repo1Branches = ResponseEntity.status(HttpStatus.OK).body("""
                [{
                    "name": "master",
                    "commit": { "sha": "sha301" }
                }, {
                    "name": "feature1",
                    "commit": { "sha": "sha302" }
                }]
                """);
        Mockito.when(restTemplate.getForEntity("https://api.github.com/repos/user1/repo1/branches", String.class)).thenReturn(repo1Branches);

        ResponseEntity<String> repo3Branches = ResponseEntity.status(HttpStatus.OK).body("""
                [{
                    "name": "master",
                    "commit": { "sha": "sha401" }
                }, {
                    "name": "feature1",
                    "commit": { "sha": "sha402" }
                }, {
                    "name": "feature2",
                    "commit": { "sha": "sha403" }
                }]
                """);
        Mockito.when(restTemplate.getForEntity("https://api.github.com/repos/user1/repo3/branches", String.class)).thenReturn(repo3Branches);

        // when
        mockMvc.perform(get("/api/github/repos")
                        .param("username", "user1")
                        .accept(MediaType.APPLICATION_JSON))

        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].repoName").value("repo1"))
                .andExpect(jsonPath("$[0].ownerLogin").value("user1"))
                .andExpect(jsonPath("$[0].branches.length()").value(2))
                .andExpect(jsonPath("$[0].branches[0].name").value("master"))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha").value("sha301"))
                .andExpect(jsonPath("$[0].branches[1].name").value("feature1"))
                .andExpect(jsonPath("$[0].branches[1].lastCommitSha").value("sha302"))

                .andExpect(jsonPath("$[1].repoName").value("repo3"))
                .andExpect(jsonPath("$[1].ownerLogin").value("user1"))
                .andExpect(jsonPath("$[1].branches.length()").value(3))
                .andExpect(jsonPath("$[1].branches[0].name").value("master"))
                .andExpect(jsonPath("$[1].branches[0].lastCommitSha").value("sha401"))
                .andExpect(jsonPath("$[1].branches[1].name").value("feature1"))
                .andExpect(jsonPath("$[1].branches[1].lastCommitSha").value("sha402"))
                .andExpect(jsonPath("$[1].branches[2].name").value("feature2"))
                .andExpect(jsonPath("$[1].branches[2].lastCommitSha").value("sha403"));
    }
}
