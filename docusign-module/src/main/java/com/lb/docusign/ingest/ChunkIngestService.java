package com.lb.docusign.ingest;

import com.lb.docusign.qa.EmbeddingClient;
import com.lb.docusign.repo.ChunkRepository;
import com.lb.docusign.ingest.PdfText;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ChunkIngestService {
    private final ChunkRepository repo;
    private final EmbeddingClient embedder;

    public ChunkIngestService(ChunkRepository repo, EmbeddingClient embedder) {
        this.repo = repo; this.embedder = embedder;
    }

    public int indexPdf(String envelopeId, String documentId, Path pdfPath) {
        String text = PdfText.extract(pdfPath);
        var chunks = PdfText.chunk(text, 1500, 200);
        int inserted = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String sha = sha256(chunk);
            if (repo.existsBySha(sha)) continue;
            float[] emb = embedder.embed(chunk);
            inserted += repo.upsert(UUID.randomUUID(), envelopeId, documentId, i, chunk, sha, emb);
        }
        return inserted;
    }

    private static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
