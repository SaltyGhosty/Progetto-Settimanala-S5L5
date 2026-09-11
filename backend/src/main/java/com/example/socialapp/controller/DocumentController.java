package com.example.socialapp.controller;

import com.example.socialapp.dto.DocumentDtos.DocumentResponse;
import com.example.socialapp.entity.Document;
import com.example.socialapp.security.SocialUserDetails;
import com.example.socialapp.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(
            @AuthenticationPrincipal SocialUserDetails principal,
            @RequestParam("file") MultipartFile file
    ) {
        DocumentResponse response = documentService.upload(principal.getDomainUser(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<DocumentResponse> listOwn(@AuthenticationPrincipal SocialUserDetails principal) {
        return documentService.listOwn(principal.getDomainUser());
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable Long id, @AuthenticationPrincipal SocialUserDetails principal) {
        return documentService.get(id, principal.getDomainUser());
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, @AuthenticationPrincipal SocialUserDetails principal) {
        Document document = documentService.findOwnedOrThrow(id, principal.getDomainUser());
        Resource resource = documentService.loadFile(id, principal.getDomainUser());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SocialUserDetails principal) {
        documentService.delete(id, principal.getDomainUser());
        return ResponseEntity.noContent().build();
    }
}
