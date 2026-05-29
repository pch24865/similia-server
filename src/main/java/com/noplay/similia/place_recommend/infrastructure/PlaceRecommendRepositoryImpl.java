package com.noplay.similia.place_recommend.infrastructure;

import com.noplay.similia.place_recommend.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlaceRecommendRepositoryImpl implements PlaceRecommendRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Place> findSimilarByImageVector(double[] vector, int limit) {
        String vectorStr = toVectorString(vector);
        String sql = """
                SELECT p.content_id, p.title, p.address1, p.image_url, p.map_x, p.map_y,
                       p.overview, p.areacode,
                       1 - (pi.image_vector <=> CAST(? AS vector)) AS similarity
                FROM places p
                JOIN place_images pi ON p.content_id = pi.content_id
                WHERE pi.image_vector IS NOT NULL
                ORDER BY pi.image_vector <=> CAST(? AS vector)
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, placeRowMapper(), vectorStr, vectorStr, limit);
    }

    @Override
    public List<Place> findSimilarByTextVector(double[] vector, int limit) {
        String vectorStr = toVectorString(vector);
        String sql = """
                SELECT content_id, title, address1, image_url, map_x, map_y,
                       overview, areacode,
                       1 - (refined_overview_vector <=> CAST(? AS vector)) AS similarity
                FROM places
                WHERE refined_overview_vector IS NOT NULL
                ORDER BY refined_overview_vector <=> CAST(? AS vector)
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, placeRowMapper(), vectorStr, vectorStr, limit);
    }

    private RowMapper<Place> placeRowMapper() {
        return (rs, rowNum) -> Place.builder()
                .contentId(rs.getString("content_id"))
                .title(rs.getString("title"))
                .address1(rs.getString("address1"))
                .imageUrl(rs.getString("image_url"))
                .mapX(rs.getDouble("map_x"))
                .mapY(rs.getDouble("map_y"))
                .overview(rs.getString("overview"))
                .areacode(rs.getString("areacode"))
                .similarity(rs.getDouble("similarity"))
                .build();
    }

    private String toVectorString(double[] vector) {
        return Arrays.stream(vector)
                .mapToObj(Double::toString)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
