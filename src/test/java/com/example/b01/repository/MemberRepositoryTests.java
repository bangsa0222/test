package com.example.b01.repository;

import com.example.b01.repository.search.MemberRepository;
import groovy.transform.ASTTest;import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@Log4j2

public class MemberRepositoryTests {
@Autowired
    private MemberRepository memberRepository;
@Autowired
    private PasswordEncoder passwordEncoder;
@Test
}
