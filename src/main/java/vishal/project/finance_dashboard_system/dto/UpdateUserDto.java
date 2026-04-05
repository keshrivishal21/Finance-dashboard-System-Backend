package vishal.project.finance_dashboard_system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {

    @NotNull(message = "User id is required")
    private Long id;

    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    private String name;

    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;
}
