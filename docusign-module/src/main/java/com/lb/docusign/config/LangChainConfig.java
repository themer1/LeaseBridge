package com.lb.docusign.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {
    @Value("${llm.openai.apiKey:}")
    private String openAiApiKey;

    @Value("${llm.openai.embeddingModel:text-embedding-3-small}")
    private String embeddingModelName;

    @Value("${llm.openai.chatModel:gpt-4o-mini}")
    private String chatModelName;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName(embeddingModelName)
                .build();
    }

    @Bean
    public ChatModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(chatModelName)
                .build();
    }
}
