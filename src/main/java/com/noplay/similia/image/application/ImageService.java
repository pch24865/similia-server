package com.noplay.similia.image.application;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.image.api.dto.ImageUploadResponseDto;
import com.noplay.similia.image.domain.Image;
import com.noplay.similia.image.domain.ImageEmbedding;
import com.noplay.similia.image.infrastructure.ImageEmbeddingRepository;
import com.noplay.similia.image.infrastructure.ImageRepository;
import com.noplay.similia.image.infrastructure.client.EmbeddingClient;
import com.noplay.similia.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final ImageEmbeddingRepository imageEmbeddingRepository;
    private final EmbeddingClient embeddingClient;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

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

        try {
            Image image = Image.builder()
                    .memberId(memberId)
                    .originalName(file.getOriginalFilename())
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .data(file.getBytes())
                    .build();

            Image savedImage = imageRepository.save(image);
            return ImageUploadResponseDto.from(savedImage);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<ImageUploadResponseDto> findAllByMember(Long memberId) {
        return imageRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(ImageUploadResponseDto::from)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Image findById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
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

    @Transactional
    public double[] getImageEmbedding(Long imageId) {
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
        return imageEmbedding.getEmbedding();
    }

    @Transactional
    public double[] getImageEmbeddingByToken(String imageToken) {
        Image image = findByToken(imageToken);
        return getImageEmbedding(image.getId());
    }

    public double[] getTextEmbedding(String text) {
        return embeddingClient.embedText(text);
    }
}
