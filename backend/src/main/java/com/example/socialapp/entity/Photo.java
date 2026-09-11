package com.example.socialapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** Nome del file su disco (UUID), diverso dal nome originale scelto dall'utente. */
    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String originalFilename;

    /** Tipo MIME rilevato dal contenuto del file (Tika), non quello dichiarato dal client. */
    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private int displayOrder;
}
