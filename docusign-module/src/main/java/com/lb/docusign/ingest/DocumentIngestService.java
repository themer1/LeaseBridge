package com.lb.docusign.ingest;

import com.docusign.esign.api.EnvelopesApi;

import com.docusign.esign.model.EnvelopeDocumentsResult;
import com.lb.docusign.config.StorageConfig;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class DocumentIngestService {
    private final DocusignClientFactory factory;
    private final StorageConfig storage;

    public DocumentIngestService(DocusignClientFactory factory, StorageConfig storage) {
        this.factory = factory; this.storage = storage;
    }

    public record SavedDoc(String path, String sha256) {}

    // Add this method to fetch the document bytes
    public byte[] fetchDocument(String envelopeId, String documentId) throws Exception {
        EnvelopesApi api = factory.envelopesApi();
        String accountId = factory.accountId();
        return api.getDocument(accountId, envelopeId, documentId);
    }

    // Add this method to save the document bytes
    public SavedDoc saveDocument(String envelopeId, String documentId, byte[] pdf) throws Exception {
        String sha = sha256(pdf);
        Path dir = Path.of(storage.root, envelopeId);
        Files.createDirectories(dir);
        Path file = dir.resolve(documentId + ".pdf");

        // If already saved with same hash, skip
        Path hashFile = dir.resolve(documentId + ".sha256");
        if (Files.exists(hashFile) && Files.readString(hashFile).equals(sha)) {
            return new SavedDoc(file.toString(), sha);
        }

        Files.write(file, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(hashFile, sha, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new SavedDoc(file.toString(), sha);
    }

    public DocumentIngestService.SavedDoc saveUploadedDocument(String envelopeId, String documentId, byte[] pdf) throws Exception {
        // Use "uploads" as the folder for uploaded documents
        String sha = sha256(("uploads/" + envelopeId + "/" + documentId).getBytes(), pdf);
        Path dir = Path.of(storage.root, "uploads", envelopeId);
        Files.createDirectories(dir);
        Path file = dir.resolve(documentId + ".pdf");
        Path hashFile = dir.resolve(documentId + ".sha256");

        if (Files.exists(hashFile) && Files.readString(hashFile).equals(sha)) {
            return new SavedDoc(file.toString(), sha);
        }

        Files.write(file, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(hashFile, sha, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new SavedDoc(file.toString(), sha);
    }

    @Deprecated
    public SavedDoc fetchAndSave(String envelopeId, String documentId) throws Exception {
        EnvelopesApi api = factory.envelopesApi();
        String accountId = factory.accountId();

        byte[] pdf = api.getDocument(accountId, envelopeId, documentId);
        String sha = sha256(pdf);
        Path dir = Path.of(storage.root, envelopeId);
        Files.createDirectories(dir);
        Path file = dir.resolve(documentId + ".pdf");

        // If already saved with same hash, skip
        Path hashFile = dir.resolve(documentId + ".sha256");
        if (Files.exists(hashFile) && Files.readString(hashFile).equals(sha)) {
            return new SavedDoc(file.toString(), sha);
        }

        Files.write(file, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(hashFile, sha, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new SavedDoc(file.toString(), sha);
    }

    public EnvelopeDocumentsResult listDocs(String envelopeId) throws Exception {
        EnvelopesApi api = factory.envelopesApi();
        return api.listDocuments(factory.accountId(), envelopeId);
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String sha256(byte[] context, byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(context);
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public String getStorageRoot() {
        return storage.root;
    }


}
