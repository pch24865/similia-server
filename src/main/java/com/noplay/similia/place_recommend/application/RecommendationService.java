package com.noplay.similia.place_recommend.application;

import com.noplay.similia.image.application.ImageService;
import com.noplay.similia.place_recommend.api.dto.RecommendationResponseDto;
import com.noplay.similia.place_recommend.infrastructure.PlaceRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PlaceRecommendRepository placeRecommendRepository;
    private final ImageService imageService;

    public List<RecommendationResponseDto> recommendByImage(Long imageId, int limit) {
        double[] embedding = imageService.getImageEmbedding(imageId);

        return placeRecommendRepository
                .findSimilarByImageVector(embedding, limit)
                .stream()
                .map(RecommendationResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<RecommendationResponseDto> recommendByText(String text, int limit) {
        double[] embedding = imageService.getTextEmbedding(text);

        return placeRecommendRepository
                .findSimilarByTextVector(embedding, limit)
                .stream()
                .map(RecommendationResponseDto::from)
                .collect(Collectors.toList());
    }
}
