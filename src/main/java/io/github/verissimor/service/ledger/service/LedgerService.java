package io.github.verissimor.service.ledger.service;

import io.github.verissimor.service.ledger.model.TransactionType;
import io.github.verissimor.service.ledger.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static io.github.verissimor.service.ledger.model.TransactionType.DEPOSIT;
import static io.github.verissimor.service.ledger.model.TransactionType.WITHDRAWAL;
import static java.math.BigDecimal.ZERO;

@Service
public class LedgerService {
  private final Map<String, List<Transaction>> transactions = new ConcurrentHashMap<>();
  private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
  private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

  public Transaction recordTransaction(String accountId, BigDecimal amount, TransactionType type) {
    validateInput(type, amount);
    transactions.putIfAbsent(accountId, Collections.synchronizedList(new ArrayList<>()));
    balances.putIfAbsent(accountId, ZERO);
    locks.putIfAbsent(accountId, new ReentrantLock());

    ReentrantLock lock = locks.get(accountId);
    lock.lock();
    try {
      BigDecimal currentBalance = getBalance(accountId);
      BigDecimal resultingBalance = currentBalance.add(amount); // relies on negative withdrawal
      if (resultingBalance.compareTo(ZERO) < 0) {
        throw new IllegalArgumentException("Insufficient balance");
      }

      UUID txnId = UUID.randomUUID();
      Instant timestamp = Instant.now();
      Transaction transaction = new Transaction(txnId, timestamp, accountId, type, amount);
      transactions.get(accountId).add(transaction);
      balances.put(accountId, resultingBalance);
      return transaction;
    } finally {
      lock.unlock();
    }
  }

  public BigDecimal getBalance(String accountId) {
    return balances.get(accountId);
  }

  public List<Transaction> getTransactions(String accountId) {
    return transactions.getOrDefault(accountId, List.of());
  }

  private void validateInput(TransactionType type, BigDecimal amount) {
    if (amount == null || amount.compareTo(ZERO) == 0) {
      throw new IllegalArgumentException("Amount can't be Zero");
    }
    if (type == DEPOSIT && amount.compareTo(ZERO) < 0) {
      throw new IllegalArgumentException("Deposit currentBalance must be positive");
    }
    if (type == WITHDRAWAL && amount.compareTo(ZERO) > 0) {
      throw new IllegalArgumentException("Withdrawal currentBalance must be negative");
    }
  }
}
