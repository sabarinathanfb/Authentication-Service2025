package com.link360.authenticationservice2025.controller;


import Exceptions.WrongPasswordException;
import com.link360.authenticationservice2025.dtos.*;
import com.link360.authenticationservice2025.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody  SignUpRequestDto signUpRequestDto) {

        SignUpResponseDto response = new SignUpResponseDto();



        try {

            if(authService.signUp(signUpRequestDto.getEmail(),signUpRequestDto.getPassword())){
                response.setRequestStatus(RequestStatus.SUCCESS);
            }else {
                response.setRequestStatus(RequestStatus.FAILURE);
            }

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (Exception e){
            response.setRequestStatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }



    }

    @GetMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {

        try {

            String token = authService.login(loginRequestDto.getEmail(),loginRequestDto.getPassword());

            LoginResponseDto response = new LoginResponseDto();

            response.setRequestStatus(RequestStatus.SUCCESS);

            MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
            headers.add("Authorization","Bearer "+token);

            return new ResponseEntity<>(
                    response,
                    headers,
                    HttpStatus.OK
            );
        }catch (Exception e){

            LoginResponseDto response = new LoginResponseDto();
            response.setRequestStatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(
                    response,
                    null,
                    HttpStatus.BAD_REQUEST
            );

        }

    }
}
