package com.example.todo.userapi.service;

import com.example.todo.auth.TokenProvider;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserRequestSignUpDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.dto.response.UserSignUpResponseDTO;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider; // OCP, DIP (SOLID 원칙) 를 지켜서 개발하는 예제

    // 회원가입 처리
    public UserSignUpResponseDTO create(UserRequestSignUpDTO dto) {

        // 자바가 봤을 때는 null 인게 에러가 아니지만, 개발자 입장에서는 오류임.
        // 그래서 에러를 throw 로 발생시키고 throws RuntimeException 으로 에러를 던져줌.
        // => 이 에러는 컨트롤러에서 받아서 try catch 로 처리해줘야 한다.
        if (dto == null) {
            throw new RuntimeException("가입 정보가 없습니다.");
        }

        String email = dto.getEmail();

        if (isDuplicate(email)) {
            log.warn("이메일이 중복되었습니다. - {}", email);
            throw new RuntimeException("중복된 이메일입니다.");
        }

        // 패스워드 인코딩
        String encodedPassword = encoder.encode(dto.getPassword());
        dto.setPassword(encodedPassword);

        // 유저 엔터티로 변환
        User user = dto.toEntity();

        User saved = userRepository.save(user);

        log.info("회원가입 정상 수행됨 - saved user = {}", saved);

        return new UserSignUpResponseDTO(saved);
    }

    public boolean isDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    // 회원 인증
    public LoginResponseDTO authenticate(LoginRequestDTO dto) {
        // 이메일을 통해 회원 정보 조회
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> {
            throw new RuntimeException("가입된 회원이 아닙니다!");
        });

        // 패스워드 일치 검증
        String rawPassword = dto.getPassword(); // 입력받은 비밀번호
        String encodedPassword = user.getPassword(); // DB 에 저장된 암호화 비밀번호

        if (!encoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        log.info("{}님 로그인 성공", user.getUserName());

        // 로그인 성공 후에 클라이언트에 뭘 리턴할 것인가?
        // JWT 를 클라이언트에게 발급해줘야 함.
        String token = tokenProvider.createToken(user);

        return new LoginResponseDTO(user, token);

    }

}
