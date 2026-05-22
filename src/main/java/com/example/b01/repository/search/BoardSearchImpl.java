package com.example.b01.repository.search;

import com.example.b01.domain.Board;
import com.example.b01.domain.QBoard;
import com.example.b01.domain.QReply;
import com.example.b01.dto.BoardImageDTO;
import com.example.b01.dto.BoardListAllDTO;
import com.example.b01.dto.BoardListReplyCountDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.stream.Collectors;

public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch{
    public BoardSearchImpl(){
        super(Board.class);
    }
    @Override
    public Page<Board> search1(Pageable pageable) {
        //QueryDSL에서 자동 생성된 Board 엔티티의 Q 클래스.
        QBoard board = QBoard.board;  //QueryDSL을 사용하여 Board 테이블을 조회할 준비
        JPQLQuery<Board> query = from(board);
        query.where(board.title.contains("1")); //title에 "1"을 포함하는 데이터만 필터링
        // SELECT * FROM board WHERE title LIKE '%1%';

        //페이징
        this.getQuerydsl().applyPagination(pageable, query);
        // SELECT * FROM board WHERE title LIKE '%1%' LIMIT 0, 10;

        List<Board> list = query.fetch();  //쿼리를 실행하고 결과를 리스트타입으로 리턴
        long count = query.fetchCount();  //검색된 데이터의 총 개수
        return null;
    }
    @Override
    public Page<Board> searchAll(String[] types, String keyword, Pageable pageable) {
        //QueryDSL에서 자동 생성된 Board 엔티티의 Q 클래스.
        QBoard board = QBoard.board;  //QueryDSL을 사용하여 Board 테이블을 조회할 준비
        JPQLQuery<Board> query = from(board);
        if( (types != null && types.length > 0) && keyword != null ){ //검색 조건과 키워드가 있다면
            BooleanBuilder booleanBuilder = new BooleanBuilder(); // (
            for(String type: types){
                switch (type){
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }//end for
            query.where(booleanBuilder);
        }//end if
        //bno > 0
        query.where(board.bno.gt(0L));
        //paging
        this.getQuerydsl().applyPagination(pageable, query);
        List<Board> list = query.fetch();
        long count = query.fetchCount();
//        return null;
        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types,
                                                             String keyword, Pageable pageable) {
        QBoard board = QBoard.board;
        QReply reply = QReply.reply;
        JPQLQuery<Board> query = from(board);
        query.leftJoin(reply).on(reply.board.eq(board));
        query.groupBy(board);
        //검색
        if( (types != null && types.length > 0) && keyword != null ){ //검색 조건과 키워드가 있다면
            BooleanBuilder booleanBuilder = new BooleanBuilder(); // (
            for(String type: types){
                switch (type){
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }//end for
            query.where(booleanBuilder);
        }//end if

        //bno > 0
        query.where(board.bno.gt(0L));
        //JPQL의 결과를 DTO로 처리
        JPQLQuery<BoardListReplyCountDTO> dtoQuery = query.select(
                    Projections.bean(BoardListReplyCountDTO.class,
                        board.bno,
                        board.title,
                        board.writer,
                        board.regDate,
                        reply.count().as("replyCount")
                ));

        //paging
        this.getQuerydsl().applyPagination(pageable, dtoQuery);
        List<BoardListReplyCountDTO> dtoList = dtoQuery.fetch();
        long count = dtoQuery.fetchCount();

//        return null;
        return new PageImpl<>(dtoList, pageable, count);
    }
//    @Override
//    public Page<BoardListReplyCountDTO> searchWithAll(String[] types, String keyword,
//                                                        Pageable pageable) {
//        QBoard board = QBoard.board;
//        QReply reply = QReply.reply;
//        // Board 엔터티를 기준으로 JPQLQuery 생성
//        JPQLQuery<Board> boardJPQLQuery = from(board);
//        // Board와 Reply를 left join
//        boardJPQLQuery.leftJoin(reply).on(reply.board.eq(board));
//        // 페이지네이션 적용
//        getQuerydsl().applyPagination(pageable, boardJPQLQuery);
//        // 쿼리 실행하여 Board 리스트 가져오기
//        List<Board> boardList = boardJPQLQuery.fetch();
//        // 가져온 Board 리스트의 각 요소 출력
//        boardList.forEach(board1 -> {
//            System.out.println(board1.getBno()); // 게시글 번호 출력
//            System.out.println(board1.getImageSet()); // 이미지 정보 출력
//            System.out.println("-----------------"); // 구분선 출력
//        });
//        return null; // 현재는 반환값이 없으므로 null 반환
//    }

    // 튜플처리 추가
    @Override
    public Page<BoardListAllDTO> searchWithAll(String[] types,
                                               String keyword,
                                               Pageable pageable) {
        QBoard board = QBoard.board;
        QReply reply = QReply.reply;
        // Board 엔터티를 기준으로 JPQLQuery 생성
        JPQLQuery<Board> boardJPQLQuery = from(board);
        // Board와 Reply를 left join
        boardJPQLQuery.leftJoin(reply).on(reply.board.eq(board));
        // 검색 조건이 있는 경우 처리
        if ((types != null && types.length > 0) && keyword != null) {
            BooleanBuilder booleanBuilder = new BooleanBuilder(); // 검색 조건을 담을 BooleanBuilder
            // 검색 타입에 따라 검색 조건 추가
            for (String type : types) {
                switch (type) {
                    case "t": // 제목 검색
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c": // 내용 검색
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w": // 작성자 검색
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            } // end for
            // 조건을 쿼리에 적용
            boardJPQLQuery.where(booleanBuilder);
        }

        // 그룹화 (게시글 기준)
        boardJPQLQuery.groupBy(board);
        // 페이지네이션 적용
        getQuerydsl().applyPagination(pageable, boardJPQLQuery);
        // 게시글과 댓글 개수를 가져오는 튜플 쿼리 생성
        JPQLQuery<Tuple> tupleJPQLQuery = boardJPQLQuery.select(board, reply.countDistinct());
        // 쿼리 실행하여 튜플 리스트 가져오기
        List<Tuple> tupleList = tupleJPQLQuery.fetch();
        // 튜플 리스트를 DTO 리스트로 변환
        List<BoardListAllDTO> dtoList = tupleList.stream().map(tuple -> {
            Board board1 = (Board) tuple.get(board); // 게시글 엔터티 가져오기
            long replyCount = tuple.get(1, Long.class); // 댓글 개수 가져오기
            // 게시글 정보를 DTO로 변환
            BoardListAllDTO dto = BoardListAllDTO.builder()
                    .bno(board1.getBno()) // 게시글 번호
                    .title(board1.getTitle()) // 제목
                    .writer(board1.getWriter()) // 작성자
                    .regDate(board1.getRegDate()) // 등록일
                    .replyCount(replyCount) // 댓글 개수
                    .build();
            //게시글 이미지 정보를 DTO로 변환
            List<BoardImageDTO> imageDTOS = board1.getImageSet().stream()
                    .sorted() // 정렬
                    .map(boardImage -> BoardImageDTO.builder()
                            .uuid(boardImage.getUuid()) // 이미지 UUID
                            .fileName(boardImage.getFileName()) // 파일 이름
                            .ord(boardImage.getOrd()) // 정렬 순서
                            .build()
                    ).collect(Collectors.toList());
            // 변환한 이미지 리스트를 DTO에 설정
            dto.setBoardImages(imageDTOS);
            return dto;
        }).collect(Collectors.toList());
        // 전체 게시글 수 가져오기
        long totalCount = boardJPQLQuery.fetchCount();
        // 페이지 객체 생성 후 반환
        return new PageImpl<>(dtoList, pageable, totalCount);
    }
}









