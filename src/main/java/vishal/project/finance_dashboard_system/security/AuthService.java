package vishal.project.finance_dashboard_system.security;


import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vishal.project.finance_dashboard_system.dto.LoginDto;
import vishal.project.finance_dashboard_system.dto.LoginResponseDto;
import vishal.project.finance_dashboard_system.dto.SignUpRequestDto;
import vishal.project.finance_dashboard_system.dto.UserDto;
import vishal.project.finance_dashboard_system.entity.User;
import vishal.project.finance_dashboard_system.entity.enums.Role;
import vishal.project.finance_dashboard_system.exception.BadRequestException;
import vishal.project.finance_dashboard_system.repository.UserRepository;
import vishal.project.finance_dashboard_system.service.UserService;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {

        Optional<User>user = userRepository.findByEmail(signUpRequestDto.getEmail());

        if (user.isPresent()) {
            throw new BadRequestException("User with email already exists");
        }

        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setRole(Role.VIEWER);
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser.setActive(true);
        User savedUser = userRepository.save(newUser);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public LoginResponseDto login(LoginDto loginDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponseDto(user.getId(), accessToken,refreshToken);
    }

    public LoginResponseDto refreshToken(String refreshToken){
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userService.getUserById(userId);
        String accessToken = jwtService.generateAccessToken(user);
        return new LoginResponseDto(user.getId(), accessToken,refreshToken);
    }
}
