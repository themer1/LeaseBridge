package com.lb.docusign.qa;

import com.lb.docusign.model.Chunk;
import com.lb.docusign.repo.ChunkRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.message.AiMessage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QaService {
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final ChunkRepository chunkRepository;

    public QaService(EmbeddingModel embeddingModel,
                     ChatModel chatModel,
                     ChunkRepository chunkRepository) {
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.chunkRepository = chunkRepository;
    }

    public QaResponse answer(String question, int topK, String envelopeIdFilter) {
        // salutation check
        String q = question.trim().toLowerCase();
        if (isSalutation(q)) {
            return new QaResponse("Hello! How can I assist you today?", List.of());
        }
        if (isThanks(q)) {
            return new QaResponse("You're welcome! Let me know if you have more questions.", List.of());
        }
        if (isGoodbye(q)) {
            return new QaResponse("Goodbye! Have a great day!", List.of());
        }

        // 1) Embed question (Response<Embedding> in 1.x)

        Embedding embedding = embeddingModel.embed(question).content();

        // 2) Retrieve top-k similar chunks
        float[] queryVec = embedding.vector(); // float[] in 1.x
        List<Chunk> chunks = chunkRepository.findTopKByEmbedding(queryVec, topK, envelopeIdFilter);

        // 3) Build context
        StringBuilder context = new StringBuilder();
        for (Chunk c : chunks) {
            context.append(String.format(
                    "Source: document_name=%s similarity=%.4f%n",
                    c.getDocumentName(), c.getCosineSimilarity()));
            context.append(c.getText()).append("\n---\n");
        }

        // 4) Call chat model with messages
        String system = "You are an assistant that answers questions using the provided context. "
                + "Cite sources inline as (document_name). "
                + "If the answer is not present in the context, try to formulate the answer based off given context and intelligence and then answer the question";

        String userContent = "Context:\n" + context + "\nQuestion: " + question;

        AiMessage aiResponse = chatModel.chat(List.of(
                SystemMessage.from(system),
                UserMessage.from(userContent)
        )).aiMessage();
        String answer = aiResponse.text();

        // 5) Citations from retrieved chunks
        List<Citation> citations = chunks.stream()
                .map(c -> new Citation(
                        snippet(c.getText()),
                        c.getDocumentName()))
                .collect(Collectors.toList());

        return new QaResponse(answer, citations);
    }

    private String snippet(String text) {
        if (text == null) return "";
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }

    private String trim(String text) {
        if (text == null) return "";
        return text.length() > 2000 ? text.substring(0, 2000) + " …" : text;
    }

    private String preview(String text) {
        if (text == null) return "";
        return text.length() > 200 ? text.substring(0, 200) + "…" : text;
    }

    private boolean isSalutation(String q) {
        return q.matches("^(hi|hello|hey|greetings|good morning|good afternoon|good evening)[!,. ]*$");
    }
    private boolean isThanks(String q) {
        return q.matches("^(thanks|thank you|thx|much appreciated)[!,. ]*$");
    }
    private boolean isGoodbye(String q) {
        return q.matches("^(bye|goodbye|see you|take care)[!,. ]*$");
    }

    // DTOs
//    public record Citation(String snippet, String documentName, String envelopeId, String documentId, int chunkIndex) {}
    public record Citation(String snippet, String documentName) {}
    public record QaResponse(String answer, List<Citation> citations) {}
}
