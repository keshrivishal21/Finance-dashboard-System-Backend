package vishal.project.finance_dashboard_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vishal.project.finance_dashboard_system.dto.CreateRecordDto;
import vishal.project.finance_dashboard_system.dto.RecordResponseDto;
import vishal.project.finance_dashboard_system.entity.enums.RecordType;
import vishal.project.finance_dashboard_system.security.JWTAuthFilter;
import vishal.project.finance_dashboard_system.security.JWTService;
import vishal.project.finance_dashboard_system.security.WebSecurityConfig;
import vishal.project.finance_dashboard_system.service.FinancialRecordService;
import vishal.project.finance_dashboard_system.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FinancialRecordController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(WebSecurityConfig.class)
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FinancialRecordService financialRecordService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createRecord_whenInvalidBody_thenReturns400() throws Exception {
        // amount is missing and note is blank -> should fail validation
        String invalidJson = "{\"type\":\"INCOME\",\"category\":\"Salary\",\"date\":\"2026-04-01\",\"note\":\"\"}";

        mockMvc.perform(post("/records")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"VIEWER"})
    void createRecord_whenViewer_thenForbidden() throws Exception {
        CreateRecordDto dto = new CreateRecordDto();
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setType(RecordType.INCOME);
        dto.setCategory("Salary");
        dto.setDate(LocalDate.of(2026, 4, 1));
        dto.setNote("April salary");

        mockMvc.perform(post("/records")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createRecord_whenAdminAndValid_thenCreated() throws Exception {
        CreateRecordDto dto = new CreateRecordDto();
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setType(RecordType.INCOME);
        dto.setCategory("Salary");
        dto.setDate(LocalDate.of(2026, 4, 1));
        dto.setNote("April salary");

        RecordResponseDto responseDto = new RecordResponseDto();
        responseDto.setId(1L);
        when(financialRecordService.createRecord(any(CreateRecordDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/records")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
