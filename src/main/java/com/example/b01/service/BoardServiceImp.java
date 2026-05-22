package com.example.b01.service;

import com.example.b01.domain.Board;
import com.example.b01.dto.*;
import com.example.b01.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class BoardServiceImp implements BoardService{
    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;

    @Override
    public Long register(BoardDTO boardDTO) {
//        Board board = modelMapper.map(boardDTO, Board.class);
        Board board = dtoToEntity(boardDTO);
        Long bno = boardRepository.save(board).getBno();
        return bno;
    }
    @Override
    public BoardDTO readOne(Long bno) {
//        Optional<Board> result = boardRepository.findById(bno);
        //board_image까지 조인 처리되는 findByWithImages()를 이용
        Optional<Board> result = boardRepository.findByIdWithImages(bno);
        Board board = result.orElseThrow();
//        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);
        BoardDTO boardDTO = entityToDTO(board);
        return boardDTO;
    }
    @Override
    public void modify(BoardDTO boardDTO) {
        Optional<Board> result = boardRepository.findById(boardDTO.getBno());
        Board board = result.orElseThrow();
        board.change(boardDTO.getTitle(), boardDTO.getContent());
        // 기존 첨부 파일(이미지) 제거
        board.clearImages();
        // 새로운 첨부 파일이 존재하는 경우 추가
        if (boardDTO.getFileNames() != null) {
            for (String fileName : boardDTO.getFileNames()) {
                // 파일명을 "_" 기준으로 분리 (UUID, 실제 파일명)
                String[] arr = fileName.split("_");
                // 게시글에 이미지 추가
                board.addImage(arr[0], arr[1]);
            }
        }
        boardRepository.save(board);
    }
    @Override
    public void remove(Long bno) {
        boardRepository.deleteById(bno);
    }
    @Override
    public PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");
        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);
        List<BoardDTO> dtoList = result.getContent().stream()
                .map(board -> modelMapper.map(board,BoardDTO.class)).
                collect(Collectors.toList());
        return PageResponseDTO.<BoardDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public PageResponseDTO<BoardListReplyCountDTO> listWithReplyCount(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");
        Page<BoardListReplyCountDTO> result = boardRepository.
                searchWithReplyCount(types, keyword, pageable);
        return PageResponseDTO.<BoardListReplyCountDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.getContent())
                .total((int) result.getTotalElements())
                .build();
    }
    @Override
    public PageResponseDTO<BoardListAllDTO> listWithAll(PageRequestDTO pageRequestDTO) {
        // 검색 타입 배열 가져오기 (제목, 내용, 작성자 등)
        String[] types = pageRequestDTO.getTypes();
        // 검색 키워드 가져오기
        String keyword = pageRequestDTO.getKeyword();
        // 페이지 정보를 생성 (정렬 기준: 게시글 번호 "bno")
        Pageable pageable = pageRequestDTO.getPageable("bno");
        // 검색 조건과 페이지 정보를 이용하여 데이터 조회
        Page<BoardListAllDTO> result = boardRepository.searchWithAll(types, keyword, pageable);
        // 조회 결과를 PageResponseDTO 객체로 변환하여 반환
        return PageResponseDTO.<BoardListAllDTO>withAll()
                .pageRequestDTO(pageRequestDTO) // 요청 페이지 정보 설정
                .dtoList(result.getContent()) // 조회된 DTO 리스트 설정
                .total((int) result.getTotalElements()) // 전체 데이터 개수 설정
                .build();

        //        return null;
    }

}









