package am.ik.servicebroker.mysql.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckTest {
	@LocalServerPort
	int port;
	WebTestClient webClient;

	@Before
	public void setup() {
		this.webClient = WebTestClient.bindToServer() //
				.baseUrl("http://localhost:" + port) //
				.build();
	}

	@Test
	public void healthCheck() {
		this.webClient.get().uri("/actuator/health") //
				.exchange() //
				.expectStatus().isEqualTo(HttpStatus.OK);
	}
}
