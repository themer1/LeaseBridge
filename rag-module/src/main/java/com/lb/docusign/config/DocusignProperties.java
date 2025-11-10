package com.lb.docusign.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "docusign")
public class DocusignProperties{

    private String baseUrl;
    private String authServer;
    private String integrationKey;
    private String userId;
    private String accountId;
    private String privateKeyB64;
    private String connectSecret;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthServer() {
        return authServer;
    }

    public void setAuthServer(String authServer) {
        this.authServer = authServer;
    }

    public String getIntegrationKey() {
        return integrationKey;
    }

    public void setIntegrationKey(String integrationKey) {
        this.integrationKey = integrationKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPrivateKeyB64() {
        return privateKeyB64;
    }

    public void setPrivateKeyB64(String privateKeyB64) {
        this.privateKeyB64 = privateKeyB64;
    }

    public String getConnectSecret() {
        return connectSecret;
    }

    public void setConnectSecret(String connectSecret) {
        this.connectSecret = connectSecret;
    }
    
}
