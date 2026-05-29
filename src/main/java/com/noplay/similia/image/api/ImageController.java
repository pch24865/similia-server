package com.noplay.similia.image.api;

import com.noplay.similia.image.api.dto.ImageUploadResponseDto;
import com.noplay.similia.image.application.ImageService;
import com.noplay.similia.image.domain.Image;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * [프론트엔드 참고용] 이미지 업로드/조회/삭제 API 컨트롤러
 * - 모든 업로드는 multipart/form-data 형식으로 요청해야 합니다.
 * - 추천용 이미지만을 다루며, 실제 AI 분석 요청은 별도의 API에서 진행됩니다.
 */
@Slf4j
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
    @Operation(summary = "내 이미지 목록 조회", description = "로그인한 유저가 업로드한 이미지 목록을 최신순으로 반환합니다.")
    @GetMapping
    public ResponseEntity<List<ImageUploadResponseDto>> getMyImages(
            @AuthenticationPrincipal String tokenMemberId) {

        Long memberId = parseMemberId(tokenMemberId);
        return ResponseEntity.ok(imageService.findAllByMember(memberId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDto> uploadImage(
            @AuthenticationPrincipal String tokenMemberId,
            @RequestParam("file") MultipartFile file) {

        log.info("이미지 업로드 요청 도착 - memberId: {}, file: {}", tokenMemberId, file != null ? file.getOriginalFilename() : "null");
        Long memberId = parseMemberId(tokenMemberId);
        ImageUploadResponseDto responseDto = imageService.upload(memberId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 이미지 원본 조회 API
     * 
     * [요청 형식]
     * - Method: GET /images/{imageId}
     * - Parameter: imageId (Long) - 업로드 시 발급받은 이미지 ID
     * 
     * [응답 형식]
     * - 웹 브라우저나 img 태그의 src 속성에 직접 사용 가능 (바이너리 데이터를 그대로 반환)
     * - Status: 200 OK
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable("imageId") Long imageId) {
        Image image = imageService.findById(imageId);

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
     * - Method: DELETE /images/{imageId}
     * - JWT 토큰 필수 (Authorization 헤더)
     * 
     * [응답 형식]
     * - Status: 204 No Content (성공적으로 삭제됨, Body 없음)
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable("imageId") Long imageId,
            @AuthenticationPrincipal String tokenMemberId) {
        
        Long memberId = parseMemberId(tokenMemberId);
        imageService.delete(memberId, imageId);
        return ResponseEntity.noContent().build();
    }

    private Long parseMemberId(String memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return Long.parseLong(memberId);
    }
}
