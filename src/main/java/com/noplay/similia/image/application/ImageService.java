package com.noplay.similia.image.application;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.image.api.dto.ImageUploadResponseDto;
import com.noplay.similia.image.domain.Image;
import com.noplay.similia.image.infrastructure.ImageRepository;
import com.noplay.similia.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 이미지 관리를 담당하는 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final AiService aiService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 프론트엔드에서 전달받은 이미지 파일을 DB(BYTEA)에 저장합니다.
     * @param memberId 회원 ID (유효성 검사 진행)
     * @param file 업로드된 이미지 파일 (용량, 타입 검사 진행)
     * @return 저장 완료된 이미지 정보 DTO
     */
    @Transactional
    public ImageUploadResponseDto upload(Long memberId, MultipartFile file) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_ID);
        }

        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // [추가된 부분 시작] AI 서버와 통신하여 벡터 데이터 받아오기
        List<Double> embedding = aiService.getImageEmbedding(file);
        log.info("AI 서버로부터 성공적으로 임베딩 값을 받아왔습니다. (벡터 크기: {})", embedding.size());
        // [추가된 부분 끝]

        try {
            Image image = Image.builder()
                    .memberId(memberId)
                    .originalName(file.getOriginalFilename())
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .data(file.getBytes())
                    .build();

            Image savedImage = imageRepository.save(image);
            // [수정된 부분] 기존에는 savedImage만 DTO로 변환했으나, 프론트엔드로 벡터값을 전달하기 위해 embedding도 함께 넘겨줌
            return ImageUploadResponseDto.from(savedImage, embedding);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public Image findByToken(String imageToken) {
        return imageRepository.findByImageToken(imageToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
    }

    @Transactional
    public void delete(Long memberId, String imageToken) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_ID);
        }

        Image image = imageRepository.findByImageToken(imageToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        imageRepository.delete(image);
    }
}
