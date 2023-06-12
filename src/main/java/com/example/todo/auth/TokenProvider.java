package com.example.todo.auth;

import com.example.todo.userapi.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 역할 : 토큰을 발급하고, 서명 위조를 검사하는 객체
 */

// 서비스의 역할이면 서비스... 등등...
// 역할이 분명하지 않으면 Component 로 빈을 등록해서 스프링이 관리하도록 하자~
@Service
public class TokenProvider {

    // 서명에 사용할 값 (512비트 이상의 랜덤 문자열로 생성 - 보안을 강화하기 위함)
    private String SECRET_KEY;

    /**
     * Json Web Token 을 생성하는 메서드
     * @param userEntity - 토큰의 내용(클레임) 에 포함될 유저 정보 <현재 로그인한 사람의 정보를 토큰의 담기>
     * @return - 생성된 json 을 암호화환 토큰값
     */
    public String createToken(User userEntity) {

        // 토큰 만료시간 생성
        Date expiry = Date.from(
            Instant.now().plus(1, ChronoUnit.DAYS) // 1 + '일' = 1일 동안 토큰 유지
        );
        
        // 토큰 생성
        /*
            {
                "iss": "서비스명", <기본>
                "exp": "2023-07-12" (만료일자), <기본>
                "iat": "2023-06-12" (발급일자), <기본>
                "email": "로그인한 사람 이메일", <내 맘대로>
                "role": "Premium" (로그인한 사람 권한), <내 맘대로>

                == 맨 밑에 서명!
            }
         */

        // 추가 클레임 정의
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEntity.getEmail());
        claims.put("role", userEntity.getRole());

        return Jwts.builder()
                // token header 에 들어갈 서명
                // SECRET_KEY 를 한번 더 암호화합니다.
                .signWith(
                        Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
                        , SignatureAlgorithm.ES512
                )
                // token payload 에 들어갈 클레임 설정
                .setIssuer("ddamddamCLUB") // iss: 발급자 정보
                .setIssuedAt(new Date()) // iat: 발급 시간
                .setExpiration(expiry) // exp: 만료 시간
                .setSubject(userEntity.getId()) // sub: 토큰을 식별할 수 있는 주요데이터
                // 나는 커스텀으로 뭔가 더 넣고싶어!
                // Map 에 추가하고싶은거 싹 넣어서 setClaims 하면 됨~
                .setClaims(claims)
                .compact();
    }
}
