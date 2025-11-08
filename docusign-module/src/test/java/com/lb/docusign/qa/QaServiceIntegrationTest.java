package com.lb.docusign.qa;

import com.docusign.esign.model.EnvelopeDocumentsResult;
import com.lb.docusign.ingest.PdfText;
import com.lb.docusign.model.Chunk;
import com.lb.docusign.repo.ChunkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;


import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QaServiceIntegrationTest {
    @Autowired
    private QaService qaService;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testAnswerQuestionOnRecentEnvelopeDocument() {
        // 1. Get envelopes changed in last 7 days
//        ResponseEntity<Map> response = restTemplate.getForEntity("/docusign/envelopes/recent", Map.class);
//        List<Map<String, String>> envelopes = (List<Map<String, String>>) response.getBody().get("envelopes");
//        if (envelopes == null || envelopes.isEmpty()) {
//            System.out.println("No envelopes changed in last 7 days.");
//            return;
//        }
//
//        String envelopeId = envelopes.get(1).get("envelopeId");
//
//        // 2. Get a document id within one of the envelopes
//        ResponseEntity<EnvelopeDocumentsResult> docsResponse = restTemplate.getForEntity(
//                "/docusign/" + envelopeId + "/documents", EnvelopeDocumentsResult.class);
//        EnvelopeDocumentsResult docsResult = docsResponse.getBody();
//        if (docsResult == null || docsResult.getEnvelopeDocuments() == null || docsResult.getEnvelopeDocuments().isEmpty()) {
//            System.out.println("No documents found in envelope: " + envelopeId);
//            return;
//        }
//        String documentId = docsResult.getEnvelopeDocuments().get(0).getDocumentId();
//
//        // 3. Download the document
//        ResponseEntity<Map> downloadResponse = restTemplate.getForEntity(
//                "/docusign/" + envelopeId + "/documents/" + documentId + "/download", Map.class);
//        String savedPath = (String) downloadResponse.getBody().get("savedPath");
//
//        // 4. Extract text using PdfText.extract
//        String text = PdfText.extract(Path.of(savedPath));
//
//        // 5. Chunk the text
//        List<String> chunks = PdfText.chunk(text, 1000, 200);
//
//        System.out.println(chunks);
        // 6. Store chunks in DB
//        for (int i = 0; i < chunks.size(); i++) {
//            Chunk chunk = new Chunk();
//            chunk.setEnvelopeId(envelopeId);
//            chunk.setDocumentId(documentId);
//            chunk.setChunkIndex(i);
//            chunk.setText(chunks.get(i));
//            chunk.setCreatedAt(Instant.now());
//            chunkRepository.save(chunk); // implement save method if not present
//        }
//
//
//        // 4. Get an answer from LLM
        QaService.QaResponse response = qaService.answer("Who is this letter addressed to?", 3, "aea89c87-8f79-487f-98e0-0efbab9a736d");
        System.out.println("here is the AI Answer: " + response.answer());

    }

    @Test
    public void testAnserAnotherQuestion() {
        QaService.QaResponse response = qaService.answer("Based off total severence compensation mentioned in this contract, how much would take home pay would be provided that receiver is in Texas?", 12, "aea89c87-8f79-487f-98e0-0efbab9a736d");
        System.out.println("here is the AI Answer: " + response.answer());
    }

    @Test
    public void createVector() {
        for (int i = 0; i < 1535; i++) {
            System.out.print("0."+i+", ");
        }
        System.out.print("0.1536");
    }
}
