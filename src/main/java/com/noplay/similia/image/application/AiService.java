package com.noplay.similia.image.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import java.util.List;

@Deprecated
@Slf4j
@Service
public class AiService {
    // @deprecated place_recommend 패키지로 따로 분리되어 사용되지 않음
    private final RestTemplate restTemplate;

    // 파이썬 서버 주소를 동적으로 할당받기 위한 설정 (기본값: http://localhost:8000)
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * FastAPI 서버와 통신하여 이미지의 벡터(임베딩) 값을 받아옵니다.
     * @param file 업로드된 이미지 파일
     * @return 임베딩 벡터 리스트 (실패 시 null 반환)
     */
    public List<Double> getImageEmbedding(MultipartFile file) {
        // 하드코딩된 주소 대신 주입받은 주소를 사용합니다.
        String url = aiServerUrl + "/embed/image";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // MultipartFile을 RestTemplate으로 전송하기 위해 ByteArrayResource로 감싸서 전달
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    // 파일명이 없을 경우를 대비하여 기본값 설정
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // AI 서버에 POST 요청 전송
            ResponseEntity<AiEmbeddingResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    AiEmbeddingResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getEmbedding();
            }

        } catch (Exception e) {
            log.error("AI 서버(FastAPI)와 통신 중 오류가 발생했습니다: {}", e.getMessage(), e);
            // global exception handler에서 처리되도록 커스텀 예외 발생
            throw new BusinessException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED);
        }
        
        throw new BusinessException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED);
    }

    // AI 서버의 JSON 응답 {"embedding": [0.012, 0.034, ...]}을 매핑하기 위한 내부 클래스
    private static class AiEmbeddingResponse {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
