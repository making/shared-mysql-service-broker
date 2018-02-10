package am.ik.servicebroker.mysql.servicebroker;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.mysql.jdbc.Driver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceBrokerTest {
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
	public void provisionBindUnbindDeprovision() {
		String serviceInstanceId = UUID.randomUUID().toString();
		String serviceBindingId = UUID.randomUUID().toString();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		EntityExchangeResult<JsonNode> binding = this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated() //
				.expectBody(JsonNode.class) //
				.consumeWith(this::assertCredentials) //
				.returnResult();

		checkServiceInstance(
				binding.getResponseBody().get("credentials").get("jdbcUrl").asText());

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();
	}

	@Test
	public void bindTwice() {
		String serviceInstanceId = UUID.randomUUID().toString();
		String serviceBindingId1 = UUID.randomUUID().toString();
		String serviceBindingId2 = UUID.randomUUID().toString();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		EntityExchangeResult<JsonNode> binding1 = this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId1) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated() //
				.expectBody(JsonNode.class) //
				.returnResult();

		checkServiceInstance(
				binding1.getResponseBody().get("credentials").get("jdbcUrl").asText());

		EntityExchangeResult<JsonNode> binding2 = this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId2) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated() //
				.expectBody(JsonNode.class) //
				.returnResult();

		checkServiceInstance(
				binding2.getResponseBody().get("credentials").get("jdbcUrl").asText());

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId1) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId2) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();
	}

	@Test
	public void updateServiceInstance() {
		String serviceInstanceId = UUID.randomUUID().toString();
		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.patch() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();
	}

	@Test
	public void conflictInProvisioning() {
		String serviceInstanceId = UUID.randomUUID().toString();
		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isEqualTo(HttpStatus.CONFLICT);

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();
	}

	@Test
	public void goneInDeprovisioning() {
		String serviceInstanceId = UUID.randomUUID().toString();
		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isEqualTo(HttpStatus.GONE);
	}

	@Test
	public void conflictInBinding() {
		String serviceInstanceId = UUID.randomUUID().toString();
		String serviceBindingId = UUID.randomUUID().toString();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isEqualTo(HttpStatus.CONFLICT);

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();
	}

	@Test
	public void goInUnbinding() {
		String serviceInstanceId = UUID.randomUUID().toString();
		String serviceBindingId = UUID.randomUUID().toString();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.put() //
				.uri("/v2/service_instances/{serviceInstanceId}/service_bindings/{serviceBindingId}",
						serviceInstanceId, serviceBindingId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isCreated();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isOk();

		this.webClient.delete() //
				.uri("/v2/service_instances/{serviceInstanceId}", serviceInstanceId) //
				.header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpiYXI=") //
				.exchange() //
				.expectStatus().isEqualTo(HttpStatus.GONE);
	}

	private void checkServiceInstance(String jdbcUrl) {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriverClass(Driver.class);
		dataSource.setUrl(jdbcUrl);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		String id = UUID.randomUUID().toString();

		jdbcTemplate
				.execute("CREATE TABLE IF NOT EXISTS demo (`id` VARCHAR(36) NOT NULL, \n" //
						+ "  PRIMARY KEY (`id`)) \n" //
						+ "  ENGINE InnoDB;");

		int count = jdbcTemplate.update("INSERT INTO demo(`id`) VALUES (?)", id);
		assertThat(count).isEqualTo(1);
	}

	private void assertCredentials(EntityExchangeResult<JsonNode> r) {
		JsonNode body = r.getResponseBody();

		JsonNode credentials = body.get("credentials");
		assertThat(credentials).isNotNull();

		JsonNode hostname = credentials.get("hostname");
		assertThat(hostname).isNotNull();
		assertThat(hostname.asText()).isEqualTo("localhost");

		JsonNode port = credentials.get("port");
		assertThat(port).isNotNull();
		assertThat(port.asInt()).isEqualTo(3306);

		JsonNode username = credentials.get("username");
		assertThat(username).isNotNull();
		assertThat(username.asText()).hasSize(16);

		JsonNode password = credentials.get("password");
		assertThat(password).isNotNull();
		assertThat(password.asText()).hasSize(32);

		JsonNode name = credentials.get("name");
		assertThat(name).isNotNull();
		assertThat(name.asText()).hasSize(35);

		JsonNode uri = credentials.get("uri");
		assertThat(uri).isNotNull();
		assertThat(uri.asText()).startsWith("mysql://");

		JsonNode jdbcUrl = credentials.get("jdbcUrl");
		assertThat(jdbcUrl).isNotNull();
		assertThat(jdbcUrl.asText()).startsWith("jdbc:mysql://");
	}
}