package io.github.verissimor.service.ledger.model.rest;

import io.github.verissimor.service.ledger.model.TransactionType;

import java.math.BigDecimal;

public record TransactionRequest(String accountId, BigDecimal amount, TransactionType type) {
}
