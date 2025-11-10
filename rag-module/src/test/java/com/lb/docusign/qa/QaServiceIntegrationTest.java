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

}
