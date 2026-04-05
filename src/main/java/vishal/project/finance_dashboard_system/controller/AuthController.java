package vishal.project.finance_dashboard_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vishal.project.finance_dashboard_system.dto.LoginDto;
import vishal.project.finance_dashboard_system.dto.LoginResponseDto;
import vishal.project.finance_dashboard_system.dto.SignUpRequestDto;
import vishal.project.finance_dashboard_system.dto.UserDto;
import vishal.project.finance_dashboard_system.security.AuthService;

import java.util.Arrays;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Value("${security.jwt.refresh-ttl-ms:604800000}")
    private long refreshTtlMs;

    @Operation(summary = "Register a new user")
    @PostMapping(value = "/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        UserDto userDto = authService.signUp(signUpRequestDto);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Login and get JWT token")
    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginDto loginDto,
            HttpServletResponse response
    ) {
        LoginResponseDto login = authService.login(loginDto);

        Cookie cookie = new Cookie("refreshToken", login.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) (refreshTtlMs / 1000));
        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDto(login.getId(), login.getAccessToken(), null));
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new AuthenticationServiceException("Refresh token not found inside the Cookies");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

        LoginResponseDto loginResponseDto = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(loginResponseDto.getId(), loginResponseDto.getAccessToken(), null));
    }
}
