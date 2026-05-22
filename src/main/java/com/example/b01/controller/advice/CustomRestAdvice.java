package com.example.b01.controller.advice;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Log4j2
public class CustomRestAdvice {
    @ExceptionHandler(BindException.class) //폼 데이터 검증(@Valid) 실패로 인해 발생하는 BindException을 처리
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED) //HTTP 응답 상태를 417 EXPECTATION_FAILED로 설정
    public ResponseEntity<Map<String, String>> handleBindException(BindException e) {
        log.error(e);
        Map<String, String> errorMap = new HashMap<>();
        if (e.hasErrors()) { //에러 발생시 에러가 발생한 필드명과 에러 코드가 errorMap에 저장
            BindingResult bindingResult = e.getBindingResult();
            bindingResult.getFieldErrors().forEach(fieldError -> {
                errorMap.put(fieldError.getField(), fieldError.getCode());
            });
        }
        return ResponseEntity.badRequest().body(errorMap);
        //필드 오류 정보를 JSON 형태로 반환하고 HTTP 400 Bad Request 응답 생성
    }

    @ExceptionHandler(DataIntegrityViolationException.class)  //DB 제약조건 오류 (FK, UNIQUE, NOT NULL 등) 발생 시 실행
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ResponseEntity<Map<String, String>> handleFKException(Exception e) {
        log.error(e);
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("time", "" + System.currentTimeMillis());
        errorMap.put("msg", "constraint fails");
        return ResponseEntity.badRequest().body(errorMap);
    }
    @ExceptionHandler(NoSuchElementException.class) //요청한 데이터가 존재하지 않을 때 발생하는 예외
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ResponseEntity<Map<String, String>> handleNoSuchElement(Exception e) {
        log.error(e);
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("time", ""+System.currentTimeMillis());
        errorMap.put("msg",  "No Such Element Exception");
        return ResponseEntity.badRequest().body(errorMap);
    }


}
