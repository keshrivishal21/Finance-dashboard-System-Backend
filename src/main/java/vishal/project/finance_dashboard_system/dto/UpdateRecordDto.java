package vishal.project.finance_dashboard_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import vishal.project.finance_dashboard_system.entity.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateRecordDto {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private RecordType type;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String note;
}
