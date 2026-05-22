package com.example.b01.controller;

import com.example.b01.dto.*;
import com.example.b01.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/board")
@Log4j2
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    @Value("${file.upload.path}")
    private String uploadPath;

    @Operation
    @GetMapping("/list")
    public void list(PageRequestDTO pageRequestDTO, Model model) {
//        PageResponseDTO<BoardDTO> responseDTO = boardService.list(pageRequestDTO);
//        PageResponseDTO<BoardListReplyCountDTO> responseDTO =
//                boardService.listWithReplyCount(pageRequestDTO);
        PageResponseDTO<BoardListAllDTO> responseDTO =
                boardService.listWithAll(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/register")
    public void registerGET(){

    }

    @PostMapping("/register")
    public String registerPost(@Valid BoardDTO boardDTO, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes){
        log.info("board POST register.......");
        if(bindingResult.hasErrors()) {
            log.info("has errors.......");
            redirectAttributes.addFlashAttribute("errors",
                                                    bindingResult.getAllErrors() );
            return "redirect:/board/register";
        }
        log.info(boardDTO);
        Long bno  = boardService.register(boardDTO);
        redirectAttributes.addFlashAttribute("result", bno);
        return "redirect:/board/list";
    }

//    @GetMapping("/read")
    @GetMapping({"/read", "/modify"})
    public void read(Long bno, PageRequestDTO pageRequestDTO, Model model){
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info(boardDTO);
        model.addAttribute("dto", boardDTO);
    }
    @PostMapping("/modify")
    public String modify( @Valid BoardDTO boardDTO,
                          BindingResult bindingResult,
                          PageRequestDTO pageRequestDTO,
                          RedirectAttributes redirectAttributes) {
        log.info("board modify post......." + boardDTO);
        if(bindingResult.hasErrors()) {
            log.info("has errors.......");
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors() );
            redirectAttributes.addAttribute("bno", boardDTO.getBno());
            return "redirect:/board/modify?"+link;
        }
        boardService.modify(boardDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("bno", boardDTO.getBno());
        return "redirect:/board/read";
    }

//    @PostMapping("/remove")
//    public String remove(Long bno, RedirectAttributes redirectAttributes) {
//        log.info("remove post.. " + bno);
//        boardService.remove(bno);
//        redirectAttributes.addFlashAttribute("result", "removed");
//        return "redirect:/board/list";
@PostMapping("/remove")
public String remove(BoardDTO boardDTO, RedirectAttributes redirectAttributes) {

    // 삭제할 게시물 번호를 가져오기
    Long bno = boardDTO.getBno();
    log.info("remove post.. " + bno);

    // 서비스 호출하여 게시물 삭제
    boardService.remove(bno);

    // 게시물이 데이터베이스에서 삭제되었으면, 첨부파일도 삭제
    log.info(boardDTO.getFileNames());

    // 삭제할 파일 리스트 가져오기
    List<String> fileNames = boardDTO.getFileNames();

    // 파일명이 존재하면 파일 삭제 처리
    if (fileNames != null && fileNames.size() > 0) {
        removeFiles(fileNames);
    }

    // 삭제 결과를 플래시 속성에 추가 (리다이렉트 시 메시지를 전달)
    redirectAttributes.addFlashAttribute("result", "removed");

    // 게시판 목록 페이지로 리다이렉트
    return "redirect:/board/list";
}

public void removeFiles(List<String> files){
    for (String fileName : files) {
        // 파일 경로를 이용해 리소스 객체 생성
        Resource resource
                = new FileSystemResource(uploadPath + File.separator
                + fileName);

        String resourceName = resource.getFilename();

        try {
            // 파일의 MIME 타입을 확인 (이미지 파일인지 여부)
            String contentType
                    = Files.probeContentType(resource.getFile().toPath());

            // 파일 삭제
            resource.getFile().delete();

            // 만약 이미지 파일이라면 섬네일도 삭제
            if (contentType.startsWith("image")) {
                File thumbnailFile = new File(uploadPath
                        + File.separator + "s_" + fileName);

                thumbnailFile.delete();
            }
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 로그 출력
            log.error(e.getMessage());
        }
    } // end for
}
}












