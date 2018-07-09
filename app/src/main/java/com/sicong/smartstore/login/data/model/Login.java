package com.sicong.smartstore.login.data.model;

public class Login {

    private String username;
    private String password;
    private String company;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String companyId) {
        this.company = companyId;
    }
}
