package com.noplay.similia.image.infrastructure;

import com.noplay.similia.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImageToken(String imageToken);
}
