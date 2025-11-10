package com.lb.docusign.repo;
import com.lb.docusign.model.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Repository
public class ChunkRepository {
    private final JdbcTemplate jdbc;

    public ChunkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Chunk> findTopKByEmbedding(float[] queryEmbedding, int k, String envelopeIdFilter) {
        String vectorLiteral = toPgVectorLiteral(queryEmbedding);

        String sql = "SELECT id, c.envelope_id, c.document_id, c.chunk_index, c.text, c.created_at, " +
                "1 - (embedding <=> " + vectorLiteral + ") AS cosine_similarity, d.document_name as document_name " +
                " FROM contract_chunks c JOIN contract_documents d\n" +
                "  ON c.envelope_id = d.envelope_id AND c.document_id = d.document_id ";

        if (envelopeIdFilter != null && !envelopeIdFilter.isBlank()) {
            sql += " WHERE c.envelope_id = ? ";
            sql += " ORDER BY embedding <=> " + vectorLiteral + " LIMIT ?";
            return jdbc.query(sql, new Object[]{envelopeIdFilter, k}, this::mapRow);
        } else {
            sql += " ORDER BY embedding <=> " + vectorLiteral + " LIMIT ?";
            return jdbc.query(sql, new Object[]{k}, this::mapRow);
        }
    }

    private Chunk mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
        Chunk c = new Chunk();
        c.setId(UUID.fromString(rs.getString("id")));
        c.setEnvelopeId(rs.getString("envelope_id"));
        c.setDocumentId(rs.getString("document_id"));
        c.setChunkIndex(rs.getInt("chunk_index"));
        c.setText(rs.getString("text"));
        c.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        c.setCosineSimilarity(rs.getDouble("cosine_similarity"));
        c.setDocumentName(rs.getString("document_name"));
        return c;
    }

    private String toPgVectorLiteral(float[] f) {
        StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append('[');
        for (int i = 0; i < f.length; i++) {
            if (i > 0) sb.append(',');
            // use double precision literal representation
            sb.append(Double.toString(f[i]));
        }
        sb.append(']');
        sb.append("'::vector");
        return sb.toString();
    }

    public int upsert(UUID id,
                      String envelopeId,
                      String documentId,
                      int chunkIndex,
                      String text,
                      String sha256,
                      float[] embedding) {
        String vec = toPgVectorLiteral(embedding);
        String sql =
                "INSERT INTO contract_chunks " +
                        "(id, envelope_id, document_id, chunk_index, text, sha256, embedding) " +
                        "VALUES (?, ?, ?, ?, ?, ?, " + vec + ") " +
                        "ON CONFLICT (sha256) DO NOTHING";
        return jdbc.update(sql, id, envelopeId, documentId, chunkIndex, text, sha256);
    }

    public boolean existsBySha(String sha256) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM contract_chunks WHERE sha256 = ?",
                Integer.class, sha256);
        return n != null && n > 0;
    }
}



//docker exec -t docusign-pg pg_dump -U docusign -d docusignrag -Fc -Z 9 \
//docker exec -it docusign-pg psql -U docusign -d docusignrag -c '\l'
//        | ssh root@37.27.202.182 "cat > /root/appdb.dump"
//
//docker exec -it postgres-db psql -U docusign -d postgres -c "CREATE DATABASE \"docusignrag\";" || true
//
//docker exec -it docusign-pg psql -U docusign -d docusignrag -c '\l'
//
//docker exec -it postgres-db psql -U docusign -d docusignrag -c 'CREATE DATABASE "docusign-pg";' || true
//docker exec -i postgres-db pg_restore -U appuser -d appdb --clean --if-exists /dev/stdin < /root/appdb.dump
//docker exec -it postgres-db psql -U appuser -d appdb -c '\dt'