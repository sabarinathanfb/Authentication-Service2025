package com.link360.authenticationservice2025.controller;


import com.link360.authenticationservice2025.dtos.SignUpRequestDto;
import com.link360.authenticationservice2025.dtos.SignUpResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


    @PostMapping("/signup")
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        return new SignUpResponseDto();

    }

    @PostMapping("/login")
    public Lo
}
