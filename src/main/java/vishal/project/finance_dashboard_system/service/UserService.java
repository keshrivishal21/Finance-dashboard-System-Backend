package vishal.project.finance_dashboard_system.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vishal.project.finance_dashboard_system.dto.CreateUserDto;
import vishal.project.finance_dashboard_system.dto.UpdateRoleDto;
import vishal.project.finance_dashboard_system.dto.UpdateUserDto;
import vishal.project.finance_dashboard_system.dto.UserDto;
import vishal.project.finance_dashboard_system.entity.User;
import vishal.project.finance_dashboard_system.entity.enums.Role;
import vishal.project.finance_dashboard_system.exception.BadRequestException;
import vishal.project.finance_dashboard_system.exception.ResourceNotFoundException;
import vishal.project.finance_dashboard_system.exception.UnauthorizedException;
import vishal.project.finance_dashboard_system.repository.UserRepository;
import vishal.project.finance_dashboard_system.utils.AppUtils;

import java.util.List;

import static vishal.project.finance_dashboard_system.utils.AppUtils.getCurrentUser;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }



    public UserDto getUser(Long id) {
        User currentUser = getCurrentUser();

        boolean isSelf = currentUser.getId() != null && currentUser.getId().equals(id);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to view this user");
        }

        User user = getUserById(id);
        return modelMapper.map(user, UserDto.class);
    }

    public List<UserDto> getAllUsers() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not allowed to view all users");
        }

        List<User> users = userRepository.findAll();
        return users.stream().map(user -> modelMapper.map(user, UserDto.class)).toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public UserDto updateUserRole(UpdateRoleDto updateRoleDto) {
        User user = getUserById(updateRoleDto.getId());
        user.setRole(updateRoleDto.getRole());
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    public UserDto createUser(CreateUserDto createUserDto) {
        String email = createUserDto.getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("User with email already exists");
        }
        User user = modelMapper.map(createUserDto, User.class);
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        user.setRole(createUserDto.getRole());
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public UserDto updateUser(UpdateUserDto updateUserDto) {
        User currentUser = getCurrentUser();

        boolean isSelf = currentUser.getId() != null && currentUser.getId().equals(updateUserDto.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to update this user");
        }

        User user = getUserById(updateUserDto.getId());
        user.setName(updateUserDto.getName());
        user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    public void deleteUser(Long id) {
        User currentUser = getCurrentUser();

        boolean isSelf = currentUser.getId() != null && currentUser.getId().equals(id);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to delete this user");
        }

        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }
}
