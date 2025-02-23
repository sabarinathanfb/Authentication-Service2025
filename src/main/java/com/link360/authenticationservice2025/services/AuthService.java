package com.link360.authenticationservice2025.services;


import Exceptions.UserAlreadyExist;
import Exceptions.UserNotExist;
import Exceptions.WrongPasswordException;
import com.link360.authenticationservice2025.models.User;
import com.link360.authenticationservice2025.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    public Boolean signUp(String email, String password) throws UserAlreadyExist {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExist("User already exist with email" + email);

        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);

        return true;

    }

    public String login(String email, String password) throws UserNotExist, WrongPasswordException {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotExist("User with Email" + email + " not found");
        }

        boolean matches =  bCryptPasswordEncoder.matches(
                password,
                user.get().getPassword()
        );

        if (matches) {
            return "token";
        }else {
            throw  new WrongPasswordException("Wrong Password");
        }

    }
}
