package com.example.socialapp.repository;

import com.example.socialapp.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwnerIdOrderByUploadedAtDesc(Long ownerId);
}
