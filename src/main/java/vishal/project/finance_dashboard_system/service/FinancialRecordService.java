package vishal.project.finance_dashboard_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vishal.project.finance_dashboard_system.dto.CreateRecordDto;
import vishal.project.finance_dashboard_system.dto.RecordResponseDto;
import vishal.project.finance_dashboard_system.dto.UpdateRecordDto;
import vishal.project.finance_dashboard_system.entity.FinancialRecord;
import vishal.project.finance_dashboard_system.entity.User;
import vishal.project.finance_dashboard_system.entity.enums.RecordType;
import vishal.project.finance_dashboard_system.entity.enums.Role;
import vishal.project.finance_dashboard_system.exception.BadRequestException;
import vishal.project.finance_dashboard_system.repository.FinancialRecordRepository;
import vishal.project.finance_dashboard_system.utils.FinancialRecordSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static vishal.project.finance_dashboard_system.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final ModelMapper modelMapper;
    private final FinancialRecordRepository financialRecordRepository;

    @Transactional
    public RecordResponseDto createRecord(CreateRecordDto createRecordDto) {
        User user = getCurrentUser();
        if (createRecordDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        FinancialRecord record = modelMapper.map(createRecordDto, FinancialRecord.class);
        record.setCreatedBy(user);
        record = financialRecordRepository.save(record);
        RecordResponseDto recordResponseDto = modelMapper.map(record, RecordResponseDto.class);
        recordResponseDto.setCreatedBy(user.getName());
        return recordResponseDto;
    }

    public Page<RecordResponseDto> getAllRecords(
            RecordType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {
        User currentUser = getCurrentUser();

        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new BadRequestException("Min amount cannot be greater than max amount");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        User createdByFilter = isPrivileged(currentUser) ? null : currentUser;
        Specification<FinancialRecord> specification = FinancialRecordSpecification.filterRecords(
                createdByFilter,
                type,
                category,
                startDate,
                endDate,
                minAmount,
                maxAmount
        );

        Pageable effectivePageable = pageable;
        if (pageable == null) {
            effectivePageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id")));
        } else {
            pageable.getSort();
            if (pageable.getSort().isUnsorted()) {
                effectivePageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"))
                );
            }
        }

        return financialRecordRepository
                .findAll(specification, effectivePageable)
                .map(record -> {
                    RecordResponseDto dto = modelMapper.map(record, RecordResponseDto.class);
                    if (record.getCreatedBy() != null) {
                        dto.setCreatedBy(record.getCreatedBy().getName());
                    }
                    return dto;
                });
    }

    private boolean isPrivileged(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.ANALYST;
    }

    public RecordResponseDto getRecordById(Long id) {
        User currentUser = getCurrentUser();
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Record not found with id: " + id));

        RecordResponseDto dto = modelMapper.map(record, RecordResponseDto.class);
        if (record.getCreatedBy() != null) {
            dto.setCreatedBy(record.getCreatedBy().getName());
        }
        return dto;
    }

    public void deleteRecordById(Long id) {
        User currentUser = getCurrentUser();
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Record not found with id: " + id));
        financialRecordRepository.delete(record);
    }

    public RecordResponseDto updateRecord(Long id, UpdateRecordDto updateRecordDto) {
        User currentUser = getCurrentUser();
        FinancialRecord record = financialRecordRepository.findById(id).orElseThrow(
                    () -> new BadRequestException("Record not found with id: " + id)
        );
        if(currentUser.getRole() != Role.ADMIN){
            throw new BadRequestException("Only admin can update records");
        }
        if(updateRecordDto.getAmount() != null && updateRecordDto.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new BadRequestException("Amount must be greater than zero");
        }
        if(updateRecordDto.getDate() != null && updateRecordDto.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the future");
        }
        record.setAmount(updateRecordDto.getAmount());
        record.setDate(updateRecordDto.getDate());
        record.setNote(updateRecordDto.getNote());
        record.setType(updateRecordDto.getType());
        record.setCategory(updateRecordDto.getCategory());
        record.setUpdatedAt(LocalDateTime.now());
        record = financialRecordRepository.save(record);
        RecordResponseDto dto = modelMapper.map(record, RecordResponseDto.class);
        if (record.getCreatedBy() != null) {
            dto.setCreatedBy(record.getCreatedBy().getName());
        }
        return dto;
    }
}
