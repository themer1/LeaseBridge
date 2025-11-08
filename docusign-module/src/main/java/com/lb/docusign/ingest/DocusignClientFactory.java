package com.lb.docusign.ingest;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.auth.OAuth;
import com.lb.docusign.config.DocusignProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Component
public class DocusignClientFactory {
    private final DocusignProperties props;
    private volatile String accessToken;
    private volatile Instant expiry = Instant.EPOCH;

    public DocusignClientFactory(DocusignProperties props) {
        this.props = props;
    }

    public EnvelopesApi envelopesApi() throws Exception {
        ApiClient apiClient = new ApiClient(props.getBaseUrl());
        ensureToken(apiClient);
        apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);
        return new EnvelopesApi(apiClient);
    }

    public String accountId() { return props.getAccountId(); }

    private synchronized void ensureToken(ApiClient apiClient) throws Exception {
        if (accessToken != null && Instant.now().isBefore(expiry.minusSeconds(60))) return;
        List<String> scopes = List.of("signature", "impersonation");
        byte[] pk = Base64.getDecoder().decode(props.getPrivateKeyB64());
        OAuth.OAuthToken tok = apiClient.requestJWTUserToken(
                props.getIntegrationKey(), props.getUserId(), scopes, pk, 3600);
        accessToken = tok.getAccessToken();
        expiry = Instant.now().plusSeconds(tok.getExpiresIn().longValue());
    }
}
