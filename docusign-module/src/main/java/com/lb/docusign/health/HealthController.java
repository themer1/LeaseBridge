package com.lb.docusign.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/docusign")
public class HealthController {
    @GetMapping("/healthz")
    public Map<String, Object> health() {
        return Map.of("status", "ok");
    }
}
