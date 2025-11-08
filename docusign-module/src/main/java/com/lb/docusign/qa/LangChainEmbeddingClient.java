package com.lb.docusign.qa;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Component
public class LangChainEmbeddingClient implements EmbeddingClient {
    private final EmbeddingModel model;

    public LangChainEmbeddingClient(EmbeddingModel model) {
        this.model = model;
    }

    @Override
    public float[] embed(String text) {
        return model.embed(text).content().vector();
    }
}
