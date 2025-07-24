package io.github.verissimor.service.ledger.controller;

import io.github.verissimor.service.ledger.SpringTestContext;
import io.github.verissimor.service.ledger.model.Transaction;
import io.github.verissimor.service.ledger.model.rest.BalanceResponse;
import io.github.verissimor.service.ledger.model.rest.TransactionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.github.verissimor.service.ledger.model.TransactionType.DEPOSIT;
import static io.github.verissimor.service.ledger.model.TransactionType.WITHDRAWAL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LedgerControllerTest extends SpringTestContext {

  @Test
  void testDepositAndBalance() {
    String accountId = "acc-001";

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("100.00"), DEPOSIT))
            .exchange()
            .expectStatus().isOk()
            .expectBody(Transaction.class)
            .value(txn -> {
              assertThat(txn.accountId()).isEqualTo(accountId);
              assertThat(txn.amount()).isEqualTo(new BigDecimal("100.00"));
              assertThat(txn.type()).isEqualTo(DEPOSIT);
            });

    client.get().uri("/api/ledger/balance/" + accountId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(BalanceResponse.class)
            .value(balance -> assertThat(balance.currentBalance()).isEqualTo("100.00"));
  }

  @Test
  void testWithdrawalAndBalance() {
    String accountId = "acc-002";

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("200.00"), DEPOSIT))
            .exchange()
            .expectStatus().isOk();

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("-50.00"), WITHDRAWAL))
            .exchange()
            .expectStatus().isOk();

    client.get().uri("/api/ledger/balance/" + accountId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(BalanceResponse.class)
            .value(balance -> assertThat(balance.currentBalance()).isEqualTo("150.00"));
  }

  @Test
  void testOverdraftFails() {
    String accountId = "acc-003";

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("100.00"), DEPOSIT))
            .exchange()
            .expectStatus().isOk();

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("-200.00"), WITHDRAWAL))
            .exchange()
            .expectStatus().is5xxServerError();
  }

  @Test
  void testTransactionHistory() {
    String accountId = "acc-004";

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("100.00"), DEPOSIT))
            .exchange()
            .expectStatus().isOk();

    client.post().uri("/api/ledger/transactions")
            .bodyValue(new TransactionRequest(accountId, new BigDecimal("-20.00"), WITHDRAWAL))
            .exchange()
            .expectStatus().isOk();

    client.get().uri("/api/ledger/transactions/" + accountId)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Transaction.class)
            .value(list -> {
              assertThat(list.size()).isEqualTo(2);
              assertThat(list.get(0).accountId()).isEqualTo(accountId);
            });
  }
}
