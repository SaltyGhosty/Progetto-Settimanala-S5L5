package com.example.socialapp.service;

import com.example.socialapp.entity.Document;
import com.example.socialapp.entity.DocumentStatus;
import com.example.socialapp.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Estrae il testo dai documenti caricati in background (thread pool in AsyncConfig), così
 * l'upload risponde subito e il client fa polling per vedere lo stato PROCESSED/FAILED.
 * I PDF vengono prima convertiti in immagini pagina per pagina (con PDFBox), perché
 * Tesseract lavora solo su immagini.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OcrService {

    private final DocumentRepository documentRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** Cartella dei modelli linguistici di Tesseract; se vuota usa il percorso di sistema. */
    @Value("${app.tesseract.datapath:}")
    private String tessDataPath;

    @Value("${app.tesseract.language:eng}")
    private String language;

    private static final int PDF_RENDER_DPI = 300;

    @Async("ocrTaskExecutor")
    public void processAsync(Long documentId) {
        markProcessing(documentId);
        try {
            String text = extractText(documentId);
            markProcessed(documentId, text);
        } catch (Throwable e) {
            // Tesseract (via JNA) può lanciare java.lang.Error, non Exception, se mancano
            // i dati della lingua: catch (Exception) da solo lascerebbe il documento bloccato.
            log.error("OCR failed for document {}", documentId, e);
            markFailed(documentId, e.getMessage());
        }
    }

    private String extractText(Long documentId) throws Exception {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Documento non trovato: " + documentId));

        Path path = Paths.get(uploadDir, "documents", doc.getStoredFilename());

        ITesseract tesseract = new Tesseract();
        if (tessDataPath != null && !tessDataPath.isBlank()) {
            tesseract.setDatapath(tessDataPath);
        }
        tesseract.setLanguage(language);

        if ("application/pdf".equals(doc.getContentType())) {
            StringBuilder sb = new StringBuilder();
            try (PDDocument pdf = PDDocument.load(path.toFile())) {
                PDFRenderer renderer = new PDFRenderer(pdf);
                for (int page = 0; page < pdf.getNumberOfPages(); page++) {
                    BufferedImage image = renderer.renderImageWithDPI(page, PDF_RENDER_DPI);
                    sb.append(tesseract.doOCR(image)).append('\n');
                }
            }
            return sb.toString().trim();
        }

        return tesseract.doOCR(path.toFile()).trim();
    }

    @Transactional
    public void markProcessing(Long documentId) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(doc);
        });
    }

    @Transactional
    public void markProcessed(Long documentId, String text) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(DocumentStatus.PROCESSED);
            doc.setOcrText(text);
            doc.setProcessedAt(Instant.now());
            documentRepository.save(doc);
        });
    }

    @Transactional
    public void markFailed(Long documentId, String errorMessage) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(DocumentStatus.FAILED);
            doc.setOcrError(errorMessage != null ? errorMessage : "Errore sconosciuto durante l'OCR");
            doc.setProcessedAt(Instant.now());
            documentRepository.save(doc);
        });
    }
}
