package com.example.socialapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** La posizione appartiene al post nel suo insieme, non alla singola foto. */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(length = 2000)
    private String caption;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Double latitude;

    private Double longitude;

    @Column(length = 255)
    private String address;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<Photo> photos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.setPost(this);
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null || address != null;
    }
}
