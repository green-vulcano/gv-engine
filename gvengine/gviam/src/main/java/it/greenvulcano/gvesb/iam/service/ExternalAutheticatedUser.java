package it.greenvulcano.gvesb.iam.service;

import org.json.JSONObject;

public class ExternalAutheticatedUser {

    private String email;
    private JSONObject authenticationInfo;

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public JSONObject getAuthenticationInfo() {

        return authenticationInfo;
    }

    public void setAuthenticationInfo(JSONObject authenticationInfo) {

        this.authenticationInfo = authenticationInfo;
    }

}