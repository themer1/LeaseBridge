package com.lb.docusign.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DocumentsRepository {
    private final JdbcTemplate jdbc;

    public DocumentsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int upsert(String envId, String docId, String name, String path, String sha, int pages, long size) {
        return jdbc.update("""
          INSERT INTO contract_documents(envelope_id, document_id, document_name, saved_path, sha256, page_count, size_bytes)
          VALUES (?, ?, ?, ?, ?, ?, ?)
          ON CONFLICT (envelope_id, document_id)
          DO UPDATE SET document_name = EXCLUDED.document_name,
                        saved_path = EXCLUDED.saved_path,
                        sha256 = EXCLUDED.sha256,
                        page_count = EXCLUDED.page_count,
                        size_bytes = EXCLUDED.size_bytes
          """, envId, docId, name, path, sha, pages, size);
    }

    public List<Map<String, Object>>  listAll() {
        return jdbc.queryForList("""
          SELECT envelope_id, document_id, document_name, page_count, size_bytes, created_at
          FROM contract_documents
          ORDER BY created_at DESC
          """);
    }
}
