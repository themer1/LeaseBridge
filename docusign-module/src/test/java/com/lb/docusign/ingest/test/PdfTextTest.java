package com.lb.docusign.ingest.test;

import com.lb.docusign.ingest.PdfText;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextTest {
    @Test
    void testExtractExecutesAllPaths() throws URISyntaxException {
        Path pdfPath = Path.of(getClass().getClassLoader()
                .getResource("contracts/test_parser/1.pdf").toURI());
        String text = PdfText.extract(pdfPath);

        assertNotNull(text, "Extracted text should not be null");
        assertFalse(text.isEmpty(), "Extracted text should not be empty");
        // Optionally, check for expected content if known
        // assertTrue(text.contains("expected string"));
    }
}