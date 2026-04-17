package com.noplay.similia.image.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploaded_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 외부 공개용 식별자 - 순차 ID 열거 공격 방지용 UUID */
    @Column(name = "image_token", nullable = false, unique = true, updatable = false, length = 36)
    private String imageToken;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "image_data", nullable = false)
    private byte[] data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.imageToken == null) {
            this.imageToken = UUID.randomUUID().toString();
        }
    }

    @Builder
    public Image(Long memberId, String originalName, String contentType, Long fileSize, byte[] data) {
        this.memberId = memberId;
        this.originalName = originalName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.data = data;
    }
}
