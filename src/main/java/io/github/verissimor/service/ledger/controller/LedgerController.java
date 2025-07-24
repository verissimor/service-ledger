package io.github.verissimor.service.ledger.controller;

import io.github.verissimor.service.ledger.model.rest.BalanceResponse;
import io.github.verissimor.service.ledger.model.rest.TransactionRequest;
import io.github.verissimor.service.ledger.model.Transaction;
import io.github.verissimor.service.ledger.service.LedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/ledger")
class LedgerController {

  private final LedgerService ledgerService;

  LedgerController(LedgerService ledgerService) {
    this.ledgerService = ledgerService;
  }

  @PostMapping("/transactions")
  public Transaction createTransaction(@RequestBody TransactionRequest request) {
    return ledgerService.recordTransaction(request.accountId(), request.amount(), request.type());
  }

  @GetMapping("/balance/{accountId}")
  public BalanceResponse getBalance(@PathVariable String accountId) {
    return new BalanceResponse(accountId, ledgerService.getBalance(accountId), Instant.now());
  }

  @GetMapping("/transactions/{accountId}")
  public List<Transaction> getTransactionHistory(@PathVariable String accountId) {
    return ledgerService.getTransactions(accountId);
  }
}
