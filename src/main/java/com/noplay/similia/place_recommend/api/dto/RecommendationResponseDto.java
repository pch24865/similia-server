package com.noplay.similia.place_recommend.api.dto;

import com.noplay.similia.place_recommend.domain.Place;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationResponseDto {
    private String contentId;
    private String title;
    private String address1;
    private String imageUrl;
    private Double mapX;
    private Double mapY;
    private String overview;
    private String areacode;
    private Double similarity;

    public static RecommendationResponseDto from(Place place) {
        return RecommendationResponseDto.builder()
                .contentId(place.getContentId())
                .title(place.getTitle())
                .address1(place.getAddress1())
                .imageUrl(place.getImageUrl())
                .mapX(place.getMapX())
                .mapY(place.getMapY())
                .overview(place.getOverview())
                .areacode(place.getAreacode())
                .similarity(place.getSimilarity())
                .build();
    }
}
