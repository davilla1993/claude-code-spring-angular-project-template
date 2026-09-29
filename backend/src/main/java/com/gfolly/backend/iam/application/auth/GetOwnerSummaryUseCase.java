package com.gfolly.quantly_backend.iam.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetOwnerSummaryUseCase {

    private final GetOwnerGlobalDashboardStatsUseCase getOwnerGlobalDashboardStatsUseCase;

    @Transactional(readOnly = true)
    public BigDecimal execute(String email, LocalDate from, LocalDate to) {
        return getOwnerGlobalDashboardStatsUseCase.execute(email, from, to).totalCA();
    }
}
