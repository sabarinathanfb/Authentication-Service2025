package com.link360.authenticationservice2025.services;


import com.link360.authenticationservice2025.Exceptions.UserAlreadyExist;
import com.link360.authenticationservice2025.Exceptions.UserNotExist;
import com.link360.authenticationservice2025.Exceptions.WrongPasswordException;
import com.link360.authenticationservice2025.models.Session;
import com.link360.authenticationservice2025.models.SessionStatus;
import com.link360.authenticationservice2025.models.User;
import com.link360.authenticationservice2025.repositories.SessionRepository;
import com.link360.authenticationservice2025.repositories.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final SecretKey key = Keys.hmacShaKeyFor(
            "thisisownserveruserserviceimplementation".getBytes(StandardCharsets.UTF_8));
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
            session.setSessionStatus(SessionStatus.ACTIVE);

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

            // Check expiration
            Date expiryAt = claims.getBody().getExpiration();

            return !expiryAt.before(new Date()); // Token expired

        } catch (JwtException | IllegalArgumentException e) {
            return false; // Invalid token
        }
    }


    public boolean logout(String token) {
        if (!validate(token)) {
            return false; // Token is invalid
        }
        Long userId = extractUserIdFromToken(token);

        Optional<Session> sessionOpt = sessionRepository.findByUserId(userId);
        if (sessionOpt.isPresent() && sessionOpt.get().getSessionStatus() == SessionStatus.ACTIVE) {
            Session session = sessionOpt.get();


            session.setSessionStatus(SessionStatus.LOGGED_OUT);
            sessionRepository.save(session);
            return true;
        }

        return false; // Session not found
    }

    public Long extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", Long.class); // Assuming you store userId in claims
        } catch (JwtException | IllegalArgumentException e) {
            return null; // Handle invalid token
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
