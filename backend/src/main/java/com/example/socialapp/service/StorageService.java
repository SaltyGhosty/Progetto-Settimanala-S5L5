package com.example.socialapp.service;

import com.example.socialapp.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/tiff", ".tiff",
            "image/bmp", ".bmp",
            "application/pdf", ".pdf"
    );

    public String store(MultipartFile file, String subdir, String detectedMimeType) {
        try {
            Path dir = Paths.get(uploadDir, subdir).normalize();
            Files.createDirectories(dir);
            String extension = EXTENSIONS.getOrDefault(detectedMimeType, "");
            String filename = UUID.randomUUID() + extension;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return filename;
        } catch (IOException e) {
            throw new StorageException("Errore nel salvataggio del file", e);
        }
    }

    public Resource loadAsResource(String subdir, String filename) {
        try {
            Path base = Paths.get(uploadDir, subdir).normalize();
            Path target = base.resolve(sanitize(filename)).normalize();
            if (!target.startsWith(base)) {
                throw new StorageException("Percorso file non valido");
            }
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new StorageException("File non trovato: " + filename);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new StorageException("Percorso file non valido", e);
        }
    }

    public Path resolvePath(String subdir, String filename) {
        return Paths.get(uploadDir, subdir).normalize().resolve(sanitize(filename)).normalize();
    }

    public void delete(String subdir, String filename) {
        try {
            Files.deleteIfExists(resolvePath(subdir, filename));
        } catch (IOException e) {
            // se il file non esiste già non è un errore grave, ignoro
        }
    }

    private String sanitize(String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new StorageException("Nome file non valido");
        }
        return filename;
    }
}
