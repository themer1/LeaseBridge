package com.lb.docusign.qa;

import org.springframework.stereotype.Component;

@Component
public interface EmbeddingClient {
    float[] embed(String text);
}
