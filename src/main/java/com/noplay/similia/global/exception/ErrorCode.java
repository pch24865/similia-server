package com.noplay.similia.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),

    // 회원가입 & 로그인
    DUPLICATE_EMAIL(409, "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(409, "이미 존재하는 닉네임입니다."),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "로그인이 만료되었습니다. 다시 로그인해주세요."),

    // --- 회원 정보 ---
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    INVALID_MEMBER_ID(400, "유효하지 않은 회원 ID입니다."),

    // 이미지 업로드
    INVALID_IMAGE_TYPE(400, "이미지 파일만 업로드할 수 있습니다."),
    EMPTY_FILE(400, "업로드된 파일이 비어 있습니다."),
    FILE_SIZE_EXCEEDED(400, "파일 크기 제한을 초과했습니다."),
    IMAGE_NOT_FOUND(404, "이미지를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAILED(500, "이미지 업로드 중 오류가 발생했습니다."),

    // 권한
    ACCESS_DENIED(403, "해당 작업에 대한 권한이 없습니다.");

    private final int code;
    private final String message;
}