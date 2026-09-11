package com.example.socialapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    /** Testo estratto dall'OCR, valorizzato in modo asincrono dopo l'upload. */
    @Column(columnDefinition = "TEXT")
    private String ocrText;

    @Column(length = 1000)
    private String ocrError;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = Instant.now();
        if (this.status == null) {
            this.status = DocumentStatus.PENDING;
        }
    }
}
