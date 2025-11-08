package com.lb.docusign.ingest;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfText {
    private PdfText() {}

    /** Extract text using PDFBox temp-file mode to keep heap low. */
    public static String extract(Path pdfPath) {
        try (PDDocument doc = PDDocument.load(
                pdfPath.toFile(),
                MemoryUsageSetting.setupTempFileOnly())) {

            PDFTextStripper stripper = new PDFTextStripper();
            // Helps with weird PDFs that “duplicate” text visually:
            stripper.setSortByPosition(true);
            try {
                // Available on newer PDFBox; if not present, just remove this line.
                stripper.setSuppressDuplicateOverlappingText(true);
            } catch (Throwable ignore) {}

            return stripper.getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract PDF text: " + pdfPath, e);
        }
    }

    /** Robust chunking: guarantees forward progress; caps pathological overlap. */
    public static List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null) text = "";
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize must be > 0");
        if (overlap < 0) overlap = 0;
        if (overlap >= chunkSize) overlap = chunkSize / 4; // guard

        int stride = chunkSize - overlap;
        List<String> chunks = new ArrayList<>(Math.max(1, text.length() / Math.max(1, stride) + 1));

        for (int start = 0; start < text.length(); start += stride) {
            int end = Math.min(text.length(), start + chunkSize);
            chunks.add(text.substring(start, end));
            if (end == text.length()) break;
        }
        return chunks;
    }

    /** Optional: page-by-page extraction for *very* large PDFs, streaming to a consumer. */
    public static void extractByPages(Path pdfPath, java.util.function.Consumer<String> onText) {
        try (PDDocument doc = PDDocument.load(
                pdfPath.toFile(), MemoryUsageSetting.setupTempFileOnly())) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            try { stripper.setSuppressDuplicateOverlappingText(true); } catch (Throwable ignore) {}

            int pages = doc.getNumberOfPages();
            for (int p = 1; p <= pages; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String pageText = stripper.getText(doc);
                onText.accept(pageText != null ? pageText : "");
            }
        } catch (IOException e) {
            throw new RuntimeException("Paged extract failed: " + pdfPath, e);
        }
    }

}
