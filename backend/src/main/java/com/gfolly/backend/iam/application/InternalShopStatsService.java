package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.api.dto.responses.OwnerGlobalDashboardResponse;
import com.gfolly.quantly_backend.iam.application.dto.ShopInternalStats;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import com.gfolly.quantly_backend.reporting.infrastructure.query.SaleReportQueries;
import com.gfolly.quantly_backend.reporting.infrastructure.query.StockReportQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service interne pour isoler les appels par boutique.
 * L'utilisation de REQUIRES_NEW ici est CRITIQUE pour forcer Hibernate à 
 * ouvrir une nouvelle session et donc à appeler le CurrentTenantIdentifierResolver
 * pour chaque boutique individuellement.
 */
@Service
@RequiredArgsConstructor
public class InternalShopStatsService {

    private final SaleReportQueries saleReportQueries;
    private final StockReportQueries stockReportQueries;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public ShopInternalStats getStatsForShop(String tenantId, LocalDate from, LocalDate to) {
        // Le contexte tenant doit être défini par l'appelant AVANT d'entrer dans cette méthode
        // pour que REQUIRES_NEW puisse l'utiliser lors de l'ouverture de la connexion.
        
        BigDecimal ca = saleReportQueries.computeCA(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        long tx = saleReportQueries.countTransactions(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        
        List<OwnerGlobalDashboardResponse.StockAlert> alerts = new ArrayList<>();
        var lowStock = stockReportQueries.findLowStockProducts();
        for (Object[] row : lowStock) {
            alerts.add(new OwnerGlobalDashboardResponse.StockAlert(
                    null, // shopName sera rempli par le parent
                    (String) row[1],
                    (BigDecimal) row[2],
                    (BigDecimal) row[3],
                    (String) row[4]
            ));
        }
        
        return new ShopInternalStats(ca, tx, alerts);
    }
}
