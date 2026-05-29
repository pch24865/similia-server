package com.noplay.similia.image.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_embedding")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_id", nullable = false, unique = true)
    private Long imageId;

    @Column(name = "embedding", columnDefinition = "float8[]", nullable = false)
    private double[] embedding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public ImageEmbedding(Long imageId, double[] embedding) {
        this.imageId = imageId;
        this.embedding = embedding;
    }
}
