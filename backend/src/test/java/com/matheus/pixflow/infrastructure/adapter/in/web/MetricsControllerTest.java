package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.matheus.pixflow.application.dto.MetricsSummaryResponse;
import com.matheus.pixflow.application.port.in.GetMetricsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetMetricsUseCase getMetricsUseCase;

    @Test
    @DisplayName("GET /api/v1/metrics should return 200 OK with metrics payload")
    void shouldReturnMetrics() throws Exception {
        MetricsSummaryResponse response = new MetricsSummaryResponse(
                50L,
                48L,
                1L,
                0L,
                1L,
                0L,
                new BigDecimal("12500.00"),
                840.5,
                98.0,
                "OPERATIONAL"
        );

        when(getMetricsUseCase.getMetrics()).thenReturn(response);

        mockMvc.perform(get("/api/v1/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(50))
                .andExpect(jsonPath("$.settledCount").value(48))
                .andExpect(jsonPath("$.systemStatus").value("OPERATIONAL"));
    }
}

