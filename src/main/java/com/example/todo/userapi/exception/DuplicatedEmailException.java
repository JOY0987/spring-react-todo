package com.example.todo.userapi.exception;

import lombok.NoArgsConstructor;

/**
 * 서비스에서 던진 에러를 잡아서 처리해주는 클래스
 */
@NoArgsConstructor
public class DuplicatedEmailException extends RuntimeException {

    // 기본 생성자 + 에러메세지를 받는 생성자
    public DuplicatedEmailException(String message) {
        super(message);
    }
}
