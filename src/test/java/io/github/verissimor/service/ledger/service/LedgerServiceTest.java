package io.github.verissimor.service.ledger.service;

import io.github.verissimor.service.ledger.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.github.verissimor.service.ledger.model.TransactionType.DEPOSIT;
import static io.github.verissimor.service.ledger.model.TransactionType.WITHDRAWAL;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;

class LedgerServiceTest {

  private final LedgerService ledgerService = new LedgerService();

  @Test
  void testDepositIncreasesBalance() {
    String accountId = "acc-1";
    ledgerService.recordTransaction(accountId, new BigDecimal("100.00"), DEPOSIT);
    assertEquals(new BigDecimal("100.00"), ledgerService.getBalance(accountId));
  }

  @Test
  void testWithdrawalDecreasesBalance() {
    String accountId = "acc-2";
    ledgerService.recordTransaction(accountId, new BigDecimal("200.00"), DEPOSIT);
    ledgerService.recordTransaction(accountId, new BigDecimal("-50.00"), WITHDRAWAL);
    assertEquals(new BigDecimal("150.00"), ledgerService.getBalance(accountId));
  }

  @Test
  void testOverdraftNotAllowed() {
    String accountId = "acc-3";
    ledgerService.recordTransaction(accountId, new BigDecimal("100.00"), DEPOSIT);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            ledgerService.recordTransaction(accountId, new BigDecimal("-200.00"), WITHDRAWAL)
    );
    assertEquals("Insufficient balance", exception.getMessage());
  }

  @Test
  void testZeroAmountThrows() {
    String accountId = "acc-4";
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            ledgerService.recordTransaction(accountId, ZERO, DEPOSIT)
    );
    assertEquals("Amount can't be Zero", exception.getMessage());
  }

  @Test
  void testNegativeDepositThrows() {
    String accountId = "acc-5";
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            ledgerService.recordTransaction(accountId, new BigDecimal("-10.00"), DEPOSIT)
    );
    assertEquals("Deposit currentBalance must be positive", exception.getMessage());
  }

  @Test
  void testTransactionHistory() {
    String accountId = "acc-6";
    ledgerService.recordTransaction(accountId, new BigDecimal("100.00"), DEPOSIT);
    ledgerService.recordTransaction(accountId, new BigDecimal("-30.00"), WITHDRAWAL);
    List<Transaction> transactions = ledgerService.getTransactions(accountId);
    assertEquals(2, transactions.size());
    assertEquals(DEPOSIT, transactions.get(0).type());
    assertEquals(WITHDRAWAL, transactions.get(1).type());
  }
}
