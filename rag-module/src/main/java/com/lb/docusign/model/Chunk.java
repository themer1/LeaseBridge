package com.lb.docusign.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class Chunk {
    private UUID id;
    private String envelopeId;
    private String documentId;
    private int chunkIndex;
    private String text;
    private Instant createdAt;
    private Double cosineSimilarity;
    private String documentName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Double getCosineSimilarity() {
        return cosineSimilarity;
    }

    public void setCosineSimilarity(Double cosineSimilarity) {
        this.cosineSimilarity = cosineSimilarity;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
