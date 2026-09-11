package com.example.socialapp.service;

import com.example.socialapp.dto.DocumentDtos.DocumentResponse;
import com.example.socialapp.entity.Document;
import com.example.socialapp.entity.User;
import com.example.socialapp.exception.ForbiddenOperationException;
import com.example.socialapp.exception.ResourceNotFoundException;
import com.example.socialapp.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final String DOCUMENTS_SUBDIR = "documents";

    private final DocumentRepository documentRepository;
    private final FileValidationService fileValidationService;
    private final StorageService storageService;
    private final OcrService ocrService;

    @Transactional
    public DocumentResponse upload(User owner, MultipartFile file) {
        String detectedType = fileValidationService.detectAndValidateDocument(file);
        String storedFilename = storageService.store(file, DOCUMENTS_SUBDIR, detectedType);

        Document document = Document.builder()
                .owner(owner)
                .storedFilename(storedFilename)
                .originalFilename(file.getOriginalFilename())
                .contentType(detectedType)
                .sizeBytes(file.getSize())
                .build();

        Document saved = documentRepository.save(document);
        // Avvio l'OCR solo dopo il commit: altrimenti il thread async può leggere il documento
        // prima che la transazione sia salvata e lo trova "non esistente", restando bloccato in PENDING.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ocrService.processAsync(saved.getId());
            }
        });
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listOwn(User owner) {
        return documentRepository.findByOwnerIdOrderByUploadedAtDesc(owner.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(Long id, User requester) {
        return toResponse(findOwnedOrThrow(id, requester));
    }

    @Transactional(readOnly = true)
    public Resource loadFile(Long id, User requester) {
        Document document = findOwnedOrThrow(id, requester);
        return storageService.loadAsResource(DOCUMENTS_SUBDIR, document.getStoredFilename());
    }

    @Transactional
    public void delete(Long id, User requester) {
        Document document = findOwnedOrThrow(id, requester);
        storageService.delete(DOCUMENTS_SUBDIR, document.getStoredFilename());
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public Document findOwnedOrThrow(Long id, User requester) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento non trovato: " + id));
        if (!document.getOwner().getId().equals(requester.getId())) {
            throw new ForbiddenOperationException("Non puoi accedere ai documenti di un altro utente.");
        }
        return document;
    }

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getStatus().name(),
                d.getOcrText(),
                d.getOcrError(),
                d.getUploadedAt(),
                d.getProcessedAt()
        );
    }
}
