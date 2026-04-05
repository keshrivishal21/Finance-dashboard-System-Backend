package vishal.project.finance_dashboard_system.dto;


import lombok.Data;
import vishal.project.finance_dashboard_system.entity.enums.Role;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean isActive;
}
