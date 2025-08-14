package org.mickss.atipera.github;

public class GitHubRepoResponse {
    private String name;
    private Owner owner;
    private boolean fork;

    public static class Owner {
        private String login;

        public String getLogin() {
            return login;
        }
        public void setLogin(String login) {
            this.login = login;
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Owner getOwner() {
        return owner;
    }
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public boolean isFork() {
        return fork;
    }
    public void setFork(boolean fork) {
        this.fork = fork;
    }
}
