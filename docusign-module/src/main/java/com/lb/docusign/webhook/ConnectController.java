package com.lb.docusign.webhook;

import com.lb.docusign.config.DocusignProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/docusign/connect")
public class ConnectController {
    private final DocusignProperties props;
    public ConnectController(DocusignProperties props) { this.props = props; }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestHeader(name="X-DocuSign-Signature-1", required=false) String sig,
                                       @RequestBody byte[] body) {
        if (!verify(sig, body, props.getConnectSecret())) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // TODO: parse JSON; get envelopeId/documents; re-fetch and re-index if changed
        return ResponseEntity.ok().build();
    }
    private boolean verify(String headerSigBase64, byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String calc = Base64.getEncoder().encodeToString(mac.doFinal(body));
            return headerSigBase64 != null && java.security.MessageDigest.isEqual(calc.getBytes(), headerSigBase64.getBytes());
        } catch (Exception e) { return false; }
    }

}
