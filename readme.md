# Simple Ledger System

This project represents a very simple ledger system and support simple deposits and withdrawals. Also allows to view current balance and the transaction history.

An overview few technical/business decisions are documented below.

---

## Building and Running

To run this project just execute:

```shell
./gradlew bootRun
```

## Running Tests

Tests and the build can be executed by: `./gradlew build`

# Rest End-points

**1. Deposit Money**
```shell
curl -X POST http://localhost:8080/api/ledger/transactions \
-H "Content-Type: application/json" \
-d '{ "accountId": "acc-123", "amount": 100.00, "type": "DEPOSIT" }'
```

**2. Withdraw Money**
```shell
curl -X POST http://localhost:8080/api/ledger/transactions \
-H "Content-Type: application/json" \
-d '{ "accountId": "acc-123", "amount": -50.00, "type": "WITHDRAWAL" }'
```

**3. View Current Balance**
```shell
curl http://localhost:8080/api/ledger/balance/acc-123
```

**4. View Transaction History**
```shell
curl http://localhost:8080/api/ledger/transactions/acc-123
```

# Technical decisions

## 1. Stack

I’ve chosen to use Java 21, as it’s the latest Long-Term Support (LTS) version.

This project was generated using Spring Initializr, with Gradle (Kotlin DSL) as the build system. The only dependency included is Spring Web.

Spring is one of the most widely adopted Java frameworks, known for its reliability and strong community support.

Although the reactive stack isn’t necessary for a simple project like this, I’m using its WebTestClient for testing. This client provides a higher-level API for writing HTTP tests, allowing you to work with objects instead of raw data.

## 2. SpringTestContext

A good practice in Spring is to use a single shared application context for all tests. This approach significantly improves test performance by avoiding repeated context initialization.

To follow this practice, I created a base test class called SpringTestContext, which loads the Spring context once. All tests that require the Spring context extend this class.

## 3. POST "/api/ledger/transactions" VS "/api/ledger/[withdrawals, deposits]"

A possible design option is to create two separate endpoints:
- `/api/ledger/withdrawals`
- `/api/ledger/deposits`

Alternatively, a single endpoint can be used:
- `/api/ledger/transactions`

I opted for the latter, as it simplifies future maintenance and makes it easier to support additional transaction types.

## 4. GitHub actions

I really don’t like the idea of something working on my machine but not elsewhere.

Since I already had a GitHub Actions workflow set up for Java projects, I simply added it to this project.

# Business decisions

## Account balances non-negative

I’ve assumed that account balances cannot be negative. While some banks support overdrafts, I chose not to include this functionality to keep the project simple and focused.

## Handling two requests at same time making balance negative

Concurrent withdrawal requests on the same account can bypass balance checks and cause a negative balance due to race conditions. To prevent this, I used a ReentrantLock per account to ensure that balance validation and updates are atomic.

This solution works in a single-instance setup, but would not be safe in a distributed system. In that case, you'd need distributed locks or database-level concurrency control to ensure consistency.

##  Ledger Double Entry

The best documentation that I know about double entry is: https://developer.squareup.com/blog/books-an-immutable-double-entry-accounting-database-service/

However, to keep the project simple, I chose not to implement a full double-entry accounting system. In a proper setup, a deposit would debit a CASH-IN or clearing account (asset) and credit the user’s account (liability), reflecting that we owe the user the money.

## Negative vs Positive Amounts

I’ve chosen to enforce positive amounts for deposits and negative amounts for withdrawals. This simplifies the domain model and makes data consumption easier, especially for downstream systems like the data lake, which can rely on the sign alone without interpreting transaction types. It also avoids the need to update consumers when new transaction types (e.g., internal transfers) are introduced.

## Caching Balance

In most systems, reads are significantly more frequent than writes. For example, a user might check their balance multiple times a day but perform only a few transactions per month.

To optimize for this pattern, I introduced a `Map<String, BigDecimal> balances` to cache account balances, enabling balance retrieval in `O(1)` time.

# Limitations

* The solution won’t work correctly in a multi-instance setup, as locking is handled in-memory and not distributed.
* Ideally a Double Entry is required for a proper Ledger system.
* There’s no linter configured, so the code may contain formatting issues or unused imports.
