package com.noplay.similia.image.infrastructure.client;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class EmbeddingClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public double[] embedText(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TextEmbeddingRequest> entity = new HttpEntity<>(new TextEmbeddingRequest(text), headers);

        try {
            EmbeddingResponse response = restTemplate.postForObject(
                    "http://localhost:3001/embed/text",
                    entity,
                    EmbeddingResponse.class);
            if (response == null || response.getEmbedding() == null) {
                throw new BusinessException(ErrorCode.EMBEDDING_FAILED);
            }
            return response.getEmbedding();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("텍스트 임베딩 서버 요청 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMBEDDING_FAILED);
        }
    }

    public double[] embedImage(byte[] imageBytes, String filename, String contentType) {
        String safeFilename = (filename != null) ? filename : "image.jpg";

        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return safeFilename;
            }
        };

        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(resource, partHeaders));

        log.info("임베딩 요청: size={}bytes, contentType={}, filename={}", imageBytes.length, contentType, safeFilename);

        try {
            EmbeddingResponse response = restTemplate.postForObject(
                    "http://localhost:3001/embed/image",
                    body,
                    EmbeddingResponse.class);

            if (response == null || response.getEmbedding() == null) {
                throw new BusinessException(ErrorCode.EMBEDDING_FAILED);
            }

            return response.getEmbedding();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("임베딩 서버 요청 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EMBEDDING_FAILED);
        }
    }
}
