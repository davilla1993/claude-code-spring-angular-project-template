package com.gfolly.backend.iam.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetOwnerSummaryUseCase {

    @Transactional(readOnly = true)
    public BigDecimal execute(String email, LocalDate from, LocalDate to) {
        // Pour le template, on retourne ZERO.
        // La logique réelle dépendra du module implémenté.
        return BigDecimal.ZERO;
    }
}
