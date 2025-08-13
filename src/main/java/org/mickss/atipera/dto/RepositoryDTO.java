package org.mickss.atipera.dto;

import java.util.List;

public class RepositoryDTO {
    private String repoName;
    private String ownerLogin;
    private List<BranchDTO> branches;

    public RepositoryDTO() {}

    public RepositoryDTO(String repoName, String ownerLogin, List<BranchDTO> branches) {
        this.repoName = repoName;
        this.ownerLogin = ownerLogin;
        this.branches = branches;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public List<BranchDTO> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchDTO> branches) {
        this.branches = branches;
    }
}
