# GitHub Repository Fetcher

Project gets list of repositories from GitHub, using GitHub API.

## Running application

- start `Main.main()`, requires **JDK** 21 and **Maven**.
- go to http://localhost:8080/api/github/repos?username=<repo-user>,
  replace repo-user with proper GitHub user account.
    - If response code is `403` then GitHub API might have temporarily blocked requests if too many sent at once.

## Integration test

Run test `GitHubControllerTest.shouldReturnRepositoriesForValidUser()`

This test runs Spring Boot context and triggers REST endpoint (GitHubController class). 
