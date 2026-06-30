package com.noplay.similia.place_recommend.api;

import com.noplay.similia.place_recommend.api.dto.RecommendationResponseDto;
import com.noplay.similia.place_recommend.api.dto.TextRecommendationRequestDto;
import com.noplay.similia.place_recommend.application.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendation", description = "여행지 추천")
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
        summary = "이미지 기반 여행지 추천",
        description = "업로드한 이미지의 임베딩 벡터와 여행지 이미지 임베딩의 코사인 유사도를 계산해 추천 여행지를 반환합니다."
    )
    @GetMapping("/image/{imageToken}")
    public ResponseEntity<List<RecommendationResponseDto>> recommendByImage(
            @Parameter(description = "업로드한 이미지 토큰 (UUID)") @PathVariable String imageToken,
            @Parameter(description = "추천 결과 수 (기본값: 10)") @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(recommendationService.recommendByImage(imageToken, limit));
    }

    @Operation(
        summary = "텍스트 기반 여행지 추천",
        description = "입력한 텍스트의 임베딩 벡터와 여행지 설명 임베딩의 코사인 유사도를 계산해 추천 여행지를 반환합니다."
    )
    @PostMapping("/text")
    public ResponseEntity<List<RecommendationResponseDto>> recommendByText(
            @Valid @RequestBody TextRecommendationRequestDto request) {

        return ResponseEntity.ok(recommendationService.recommendByText(request.getText(), request.getLimit()));
    }
}
