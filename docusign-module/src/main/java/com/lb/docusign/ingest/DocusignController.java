package com.lb.docusign.ingest;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.model.Envelope;
import com.docusign.esign.model.EnvelopesInformation;
import com.lb.docusign.repo.DocumentsRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class DocusignController {
    private final DocusignClientFactory factory;
    DocumentIngestService ingest;
    ChunkIngestService chunkIngest;
    private final DocumentsRepository docsRepo;

    public DocusignController(DocusignClientFactory factory,
                              DocumentIngestService ingest,
                              ChunkIngestService chunkIngest,
                              DocumentsRepository docsRepo) {
        this.factory = factory;
        this.ingest = ingest;
        this.chunkIngest = chunkIngest;
        this.docsRepo = docsRepo;
    }

    @GetMapping("docusign/envelopes/recent")
    public Map<String, Object> recent() throws Exception {
        try {
            EnvelopesApi api = factory.envelopesApi();
            String accountId = factory.accountId();

            EnvelopesApi.ListStatusChangesOptions opts = api.new ListStatusChangesOptions();
            opts.setFromDate(OffsetDateTime.now().minusDays(7).toString());
            EnvelopesInformation info = api.listStatusChanges(accountId, opts);
            List<Map<String, String>> list = info.getEnvelopes().stream()
                    .map(e -> Map.of("envelopeId", e.getEnvelopeId(), "status", e.getStatus(), "subject", e.getEmailSubject()))
                    .toList();

            return Map.of("count", list.size(), "envelopes", list);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch recent envelopes", e);
        }
    }

    @GetMapping("/docusign/{envelopeId}/documents")
    public Object docs(@org.springframework.web.bind.annotation.PathVariable String envelopeId) throws Exception {
        return ingest.listDocs(envelopeId);
    }

    @GetMapping("/docusign/{envelopeId}/documents/{documentId}/download")
    public Object download(@org.springframework.web.bind.annotation.PathVariable String envelopeId,
                           @org.springframework.web.bind.annotation.PathVariable String documentId) throws Exception {
        byte[] pdf = ingest.fetchDocument(envelopeId, documentId);
        var saved = ingest.saveDocument(envelopeId, documentId, pdf);
        int chunksInserted = chunkIngest.indexPdf(envelopeId, documentId, Path.of(saved.path()));
        return Map.of("savedPath", saved.path(), "sha256", saved.sha256());
    }

    /*
     * This function does main upload
     *  - Check if file is not empty or is not pdf throw appropraite exceptions
     *  - store file on the file-system
     *  -
     */
    @PostMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        // Don’t rely only on content-type; also sniff PDF magic header
        if (!isPdf(file)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        // 2) Generate synthetic IDs (prefix envelope with 'u-' to distinguish uploads)
        String envelopeId = "u-" + UUID.randomUUID();
        String documentId = UUID.randomUUID().toString();

        // 3) Persist PDF to storage (reuses your service + SHA-256)
        byte[] pdf = file.getBytes();
        var saved = ingest.saveUploadedDocument(envelopeId, documentId, pdf);

        // 4) Gather simple metadata (name, size, page count) and upsert it
        String originalName = (file.getOriginalFilename() != null) ? file.getOriginalFilename() : documentId + ".pdf";
        int pageCount;
        try (PDDocument doc = PDDocument.load(pdf)) {
            pageCount = doc.getNumberOfPages();
        }
        docsRepo.upsert(envelopeId, documentId, originalName, saved.path(), saved.sha256(), pageCount, pdf.length);

        // 5) Index (extract → chunk → embed → upsert chunks)
        int chunksInserted = 0;
        try {
            chunksInserted = chunkIngest.indexPdf(envelopeId, documentId, Path.of(saved.path()));
        } catch (Exception e) {
            throw new Exception(e);
        }

        // 6) Response
        return Map.of(
                "envelopeId", envelopeId,
                "documentId", documentId,
                "fileName", originalName,
                "savedPath", saved.path(),
                "sha256", saved.sha256(),
                "pageCount", pageCount,
                "sizeBytes", pdf.length,
                "chunksInserted", chunksInserted
        );
    }

    private boolean isPdf(MultipartFile file) throws Exception {
        // quick magic check for "%PDF-"
        byte[] head = file.getInputStream().readNBytes(5);
        String magic = new String(head);
        if (!magic.startsWith("%PDF-")) return false;

        // and content-type hint
        String ct = file.getContentType();
        return ct != null && ct.toLowerCase().contains("pdf");
    }
}
