package io.github.verissimor.service.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract public class SpringTestContext {

  @LocalServerPort
  private int port;
  protected WebTestClient client;

  @BeforeEach
  void setup() {
    this.client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

}
