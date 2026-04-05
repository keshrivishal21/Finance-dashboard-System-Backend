package vishal.project.finance_dashboard_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vishal.project.finance_dashboard_system.dto.CreateRecordDto;
import vishal.project.finance_dashboard_system.dto.RecordResponseDto;
import vishal.project.finance_dashboard_system.dto.UpdateRecordDto;
import vishal.project.finance_dashboard_system.entity.enums.RecordType;
import vishal.project.finance_dashboard_system.service.FinancialRecordService;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/records")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<Page<RecordResponseDto>> getAllRecords(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                financialRecordService.getAllRecords(type, category, startDate, endDate, minAmount, maxAmount, pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<RecordResponseDto> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(financialRecordService.getRecordById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<RecordResponseDto> createRecord(@Valid @RequestBody CreateRecordDto createRecordDto) {
        return new ResponseEntity<>(financialRecordService.createRecord(createRecordDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<RecordResponseDto> updateRecordById(
            @PathVariable Long id, @Valid @RequestBody UpdateRecordDto updateRecordDto
    ) {
        return ResponseEntity.ok(financialRecordService.updateRecord(id, updateRecordDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteRecordById(@PathVariable Long id) {
        financialRecordService.deleteRecordById(id);
        return ResponseEntity.noContent().build();
    }

}
