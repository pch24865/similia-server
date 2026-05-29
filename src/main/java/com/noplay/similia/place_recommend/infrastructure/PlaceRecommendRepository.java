package com.noplay.similia.place_recommend.infrastructure;

import com.noplay.similia.place_recommend.domain.Place;

import java.util.List;

public interface PlaceRecommendRepository {
    List<Place> findSimilarByImageVector(double[] vector, int limit);
    List<Place> findSimilarByTextVector(double[] vector, int limit);
}
