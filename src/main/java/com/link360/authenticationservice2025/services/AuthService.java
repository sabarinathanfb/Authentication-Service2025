package com.link360.authenticationservice2025.services;


import com.link360.authenticationservice2025.Exceptions.UserAlreadyExist;
import com.link360.authenticationservice2025.Exceptions.UserNotExist;
import com.link360.authenticationservice2025.Exceptions.WrongPasswordException;
import com.link360.authenticationservice2025.models.Session;
import com.link360.authenticationservice2025.models.User;
import com.link360.authenticationservice2025.repositories.SessionRepository;
import com.link360.authenticationservice2025.repositories.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final SessionRepository sessionRepository;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
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
            String token =  createJwtToken(
                    user.get().getId(),
                    new ArrayList<>(),
                    user.get().getEmail()
            );

            Session session = new Session();
            session.setToken(token);
            session.setUser(user.get());

            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            Date expiration = calendar.getTime();

            session.setExpiringAt(expiration);

            sessionRepository.save(session);
            return token;
        }else {
            throw  new WrongPasswordException("Wrong Password");
        }



    }

    public boolean validate(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    private String createJwtToken(Long userId, List<String> roles,String email) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("roles", roles);
        map.put("email", email);

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date expiration = calendar.getTime();

        String token = Jwts.builder()
                .setClaims(map)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key).compact();

        return token;
    }
}
