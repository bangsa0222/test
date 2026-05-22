package com.example.b01.repository.search;

import com.example.b01.domain.Board;
import com.example.b01.dto.BoardListAllDTO;
import com.example.b01.dto.BoardListReplyCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearch {
    Page<Board> search1(Pageable pageable);
    Page<Board> searchAll(String[] types, String keyword, Pageable pageable);
    Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types,
                                                      String keyword,
                                                      Pageable pageable);
//    Page<BoardListReplyCountDTO> searchWithAll(String[] types,
    Page<BoardListAllDTO> searchWithAll(String[] types,
                                    String keyword,
                                    Pageable pageable);

}
