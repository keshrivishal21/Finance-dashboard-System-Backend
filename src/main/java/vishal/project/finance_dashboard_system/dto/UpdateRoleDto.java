package vishal.project.finance_dashboard_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vishal.project.finance_dashboard_system.entity.enums.Role;

@Data
public class UpdateRoleDto {

    @NotNull(message = "User id is required")
    private Long id;

    @NotNull(message = "Role is required")
    private Role role;
}
