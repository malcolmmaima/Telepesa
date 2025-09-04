package com.maelcolium.telepesa.loan.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maelcolium.telepesa.loan.dto.CreateLoanRequest;
import com.maelcolium.telepesa.loan.dto.LoanDto;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerWebTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TestController testController;

    @BeforeEach
    void setup() {
        this.testController = new TestController();
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(testController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .setValidator(new LocalValidatorFactoryBean())
            .build();
    }

    @Test
    @DisplayName("Validation error on createLoan should return 400 with details")
    void createLoan_WithMissingRequiredField_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{" +
            "\"userId\":100," +
            "\"accountNumber\":\"ACC001\"," +
            "\"loanType\":\"PERSONAL\"," +
            "\"interestRate\":\"12.5000\"," +
            "\"termMonths\":24" +
            "}";

        mockMvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.validationErrors.principalAmount").exists());
    }

    @Test
    @DisplayName("LoanNotFoundException should map to 404")
    void getLoan_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Loan Not Found"))
            .andExpect(jsonPath("$.message").value("Loan not found"));
    }

    @Test
    @DisplayName("LoanOperationException should map to 400")
    void updateLoanStatus_WithOperationError_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/test/operation"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Loan Operation Error"))
            .andExpect(jsonPath("$.message").value("Invalid operation"));
    }

    @Test
    @DisplayName("Unhandled exception should map to 500")
    void getAllLoans_WithUnhandledException_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test/general"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Happy path sanity (controller wiring) to ensure slice is valid")
    void createLoan_HappyPath_ShouldReturnCreated() throws Exception {
        CreateLoanRequest request = CreateLoanRequest.builder()
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("12.5000"))
            .termMonths(24)
            .build();

        LoanDto dto = LoanDto.builder()
            .id(1L)
            .loanNumber("PL202412001234")
            .userId(100L)
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.PENDING)
            .principalAmount(new BigDecimal("50000.00"))
            .applicationDate(LocalDate.now())
            .build();

        mockMvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new LoanNotFoundException("Loan not found");
        }

        @PutMapping("/operation")
        public void operation() {
            throw new LoanOperationException("Invalid operation");
        }

        @GetMapping("/general")
        public void general() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/validate")
        public ResponseEntity<LoanDto> validate(@RequestBody @jakarta.validation.Valid CreateLoanRequest request) {
            return ResponseEntity.ok().build();
        }
    }
}


