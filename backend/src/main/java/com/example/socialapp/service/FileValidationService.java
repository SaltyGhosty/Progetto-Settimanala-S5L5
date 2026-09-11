package com.example.socialapp.service;

import com.example.socialapp.exception.InvalidFileException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Controlla il tipo reale dei file caricati leggendo il contenuto (Apache Tika),
 * non il Content-Type dichiarato dal client o l'estensione, facilmente falsificabili.
 */
@Service
public class FileValidationService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/tiff", "image/bmp"
    );

    private static final long MAX_IMAGE_SIZE_BYTES = 15L * 1024 * 1024;
    private static final long MAX_DOCUMENT_SIZE_BYTES = 25L * 1024 * 1024;

    private final Tika tika = new Tika();

    public String detectAndValidateImage(MultipartFile file) {
        String detected = detect(file);
        if (!ALLOWED_IMAGE_TYPES.contains(detected)) {
            throw new InvalidFileException(
                    "Formato immagine non supportato (" + detected + "). Formati ammessi: JPEG, PNG, WEBP.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidFileException("L'immagine supera la dimensione massima di 15MB.");
        }
        return detected;
    }

    public String detectAndValidateDocument(MultipartFile file) {
        String detected = detect(file);
        if (!ALLOWED_DOCUMENT_TYPES.contains(detected)) {
            throw new InvalidFileException(
                    "Formato documento non supportato (" + detected + "). Formati ammessi: PDF, JPEG, PNG, TIFF, BMP.");
        }
        if (file.getSize() > MAX_DOCUMENT_SIZE_BYTES) {
            throw new InvalidFileException("Il documento supera la dimensione massima di 25MB.");
        }
        return detected;
    }

    private String detect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File mancante o vuoto.");
        }
        try (InputStream in = file.getInputStream()) {
            return tika.detect(in, file.getOriginalFilename());
        } catch (IOException e) {
            throw new InvalidFileException("Impossibile leggere il file caricato.");
        }
    }
}
