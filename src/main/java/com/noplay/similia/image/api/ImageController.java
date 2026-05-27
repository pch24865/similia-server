package com.noplay.similia.image.api;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.image.api.dto.ImageUploadResponseDto;
import com.noplay.similia.image.application.ImageService;
import com.noplay.similia.image.domain.Image;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * [프론트엔드 참고용] 이미지 업로드/조회/삭제 API 컨트롤러
 * - 모든 업로드는 multipart/form-data 형식으로 요청해야 합니다.
 * - 추천용 이미지만을 다루며, 실제 AI 분석 요청은 별도의 API에서 진행됩니다.
 */
@Tag(name = "Image", description = "이미지 업로드")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 업로드 API
     * 
     * [요청 형식]
     * - Method: POST /images
     * - Content-Type: multipart/form-data
     * - Request 파라미터:
     *   1. file (File): 업로드할 이미지 파일 (필수, 5MB 이하)
     *   * JWT 토큰 필수 (Authorization 헤더)
     * 
     * [응답 형식]
     * - Status: 201 Created
     * - Body: ImageUploadResponseDto (업로드된 이미지의 id, 생성일 등)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDto> uploadImage(
            @AuthenticationPrincipal String tokenMemberId,
            @RequestParam("file") MultipartFile file) {

        Long memberId = parseMemberId(tokenMemberId);
        ImageUploadResponseDto responseDto = imageService.upload(memberId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 이미지 원본 조회 API
     * 
     * [요청 형식]
     * - Method: GET /images/{imageToken}
     * - Parameter: imageToken (UUID) - 업로드 시 발급받은 이미지 토큰
     * - JWT 토큰 필수 (Authorization 헤더)
     * 
     * [응답 형식]
     * - 웹 브라우저나 img 태그의 src 속성에 직접 사용 가능 (바이너리 데이터를 그대로 반환)
     * - Status: 200 OK
     */
    @GetMapping("/{imageToken}")
    public ResponseEntity<byte[]> getImage(@PathVariable("imageToken") String imageToken) {
        Image image = imageService.findByToken(imageToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(image.getData());
    }

    /**
     * 이미지 삭제 API
     * 
     * [요청 형식]
     * - Method: DELETE /images/{imageToken}
     * - JWT 토큰 필수 (Authorization 헤더)
     * 
     * [응답 형식]
     * - Status: 204 No Content (성공적으로 삭제됨, Body 없음)
     */
    @DeleteMapping("/{imageToken}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable("imageToken") String imageToken,
            @AuthenticationPrincipal String tokenMemberId) {
        
        Long memberId = parseMemberId(tokenMemberId);
        imageService.delete(memberId, imageToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT에서 추출한 memberId(String)를 Long으로 변환합니다.
     * - null이면 JWT가 없거나 만료된 것이므로 401(INVALID_TOKEN) 반환
     * - BusinessException을 통해 GlobalExceptionHandler가 일관된 에러 형식으로 처리합니다.
     */
    private Long parseMemberId(String memberId) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return Long.parseLong(memberId);
    }
}
