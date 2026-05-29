package com.noplay.similia.place_recommend.application;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.image.domain.Image;
import com.noplay.similia.image.domain.ImageEmbedding;
import com.noplay.similia.image.infrastructure.ImageEmbeddingRepository;
import com.noplay.similia.image.infrastructure.ImageRepository;
import com.noplay.similia.image.infrastructure.client.EmbeddingClient;
import com.noplay.similia.place_recommend.api.dto.RecommendationResponseDto;
import com.noplay.similia.place_recommend.infrastructure.PlaceRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PlaceRecommendRepository placeRecommendRepository;
    private final EmbeddingClient embeddingClient;
    private final ImageEmbeddingRepository imageEmbeddingRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public List<RecommendationResponseDto> recommendByImage(Long imageId, int limit) {
        ImageEmbedding imageEmbedding = imageEmbeddingRepository.findByImageId(imageId)
                .orElseGet(() -> {
                    Image image = imageRepository.findById(imageId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
                    double[] embedding = embeddingClient.embedImage(
                            image.getData(), image.getOriginalName(), image.getContentType());
                    return imageEmbeddingRepository.save(
                            ImageEmbedding.builder()
                                    .imageId(imageId)
                                    .embedding(embedding)
                                    .build());
                });

        return placeRecommendRepository
                .findSimilarByImageVector(imageEmbedding.getEmbedding(), limit)
                .stream()
                .map(RecommendationResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<RecommendationResponseDto> recommendByText(String text, int limit) {
        double[] embedding = embeddingClient.embedText(text);

        return placeRecommendRepository
                .findSimilarByTextVector(embedding, limit)
                .stream()
                .map(RecommendationResponseDto::from)
                .collect(Collectors.toList());
    }
}
