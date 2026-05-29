package com.noplay.similia.image.infrastructure;

import com.noplay.similia.image.domain.ImageEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageEmbeddingRepository extends JpaRepository<ImageEmbedding, Long> {
    Optional<ImageEmbedding> findByImageId(Long imageId);
}
