package am.ik.servicebroker.mysql.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "service-broker.admin")
@Component
public class ServiceBrokerAdmin {
	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = new Pbkdf2PasswordEncoder().encode(password);
	}

	public UserDetails asUserDetails() {
		return User.withUsername(this.username) //
				.password("{pbkdf2}" + this.password) //
				.roles("ADMIN") //
				.build();
	}
}
