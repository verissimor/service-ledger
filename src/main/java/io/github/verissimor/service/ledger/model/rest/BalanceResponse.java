package io.github.verissimor.service.ledger.model.rest;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(String accountId, BigDecimal currentBalance, Instant generatedAt) {
}
