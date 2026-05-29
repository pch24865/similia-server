package com.noplay.similia.place_recommend.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Place {
    private String contentId;
    private String title;
    private String address1;
    private String imageUrl;
    private Double mapX;
    private Double mapY;
    private String overview;
    private String areacode;
    private Double similarity;
}
