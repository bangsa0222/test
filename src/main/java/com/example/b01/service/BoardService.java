package com.example.b01.service;

import com.example.b01.domain.Board;
import com.example.b01.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public interface BoardService {
    Long register(BoardDTO boardDTO);
    BoardDTO readOne(Long bno);
    void modify(BoardDTO boardDTO);
    void remove(Long bno);
    PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO);
    //댓글의 숫자까지 처리
    PageResponseDTO<BoardListReplyCountDTO> listWithReplyCount(PageRequestDTO pageRequestDTO);
    //게시글의 이미지와 댓글의 숫자까지 처리
    PageResponseDTO<BoardListAllDTO> listWithAll(PageRequestDTO pageRequestDTO);
    // BoardDTO를 Board 엔터티로 변환하는 메서드
    default Board dtoToEntity(BoardDTO boardDTO) {
        // Board 객체 생성 (빌더 패턴 사용)
        Board board = Board.builder()
                .bno(boardDTO.getBno()) // 게시글 번호 설정
                .title(boardDTO.getTitle()) // 제목 설정
                .content(boardDTO.getContent()) // 내용 설정
                .writer(boardDTO.getWriter()) // 작성자 설정
                .build();
        // 파일명이 존재하는 경우 이미지 정보를 Board에 추가
        if (boardDTO.getFileNames() != null) {
            boardDTO.getFileNames().forEach(fileName -> {
                // 파일명을 "_" 기준으로 분리 (UUID, 실제 파일명)
                String[] arr = fileName.split("_");
                // 분리된 정보를 이용하여 Board에 이미지 추가
                board.addImage(arr[0], arr[1]);
            });
        }
        return board; // 변환된 Board 객체 반환
    }
    // Board 엔터티를 BoardDTO로 변환하는 메서드
    default BoardDTO entityToDTO(Board board) {
        // BoardDTO 객체 생성 (빌더 패턴 사용)
        BoardDTO boardDTO = BoardDTO.builder()
                .bno(board.getBno()) // 게시글 번호 설정
                .title(board.getTitle()) // 제목 설정
                .content(board.getContent()) // 내용 설정
                .writer(board.getWriter()) // 작성자 설정
                .regDate(board.getRegDate()) // 등록일 설정
                .modDate(board.getModDate()) // 수정일 설정
                .build();
        // 게시글에 포함된 이미지 파일명을 리스트로 변환
        List<String> fileNames = board.getImageSet().stream()
                .sorted() // 정렬
                .map(boardImage ->
                   // "UUID_파일명" 형식으로 변환
                   boardImage.getUuid() + "_" + boardImage.getFileName())
                .collect(Collectors.toList());
        // 변환된 파일명 리스트를 DTO에 설정
        boardDTO.setFileNames(fileNames);
        return boardDTO; // 변환된 BoardDTO 객체 반환
    }

}
