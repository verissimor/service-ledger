package io.github.verissimor.service.ledger.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        Instant timestamp,
        String accountId,
        TransactionType type,
        BigDecimal amount
) {}