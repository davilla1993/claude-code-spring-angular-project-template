package com.gfolly.quantly_backend.iam.domain;

import com.gfolly.quantly_backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Getter
@Setter
public class Tenant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private String plan = "FREE";

    @Column(name = "tva_rate", precision = 5, scale = 2)
    private BigDecimal tvaRate = BigDecimal.valueOf(18.00); // Taux par défaut au Togo

    @Column(name = "banknotes")
    private String banknotes = "500,1000,2000,5000,10000";

    @Column(name = "coins")
    private String coins = "25,50,100,200";

    @Column(name = "currency_code", length = 3)
    private String currencyCode = "XOF";

    @Column
    private String country;

    @Column
    private String address;

    @Column
    private String phone;

    @Column(name = "logo_url")
    private String logoUrl;

    /** Date d'expiration/suspension automatique. null = pas d'expiration. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public boolean canAccess() {
        if (!Boolean.TRUE.equals(active)) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        return true;
    }

    public String getSubscriptionStatus() {
        return Boolean.TRUE.equals(active) ? "ACTIVE" : "INACTIVE";
    }
}
