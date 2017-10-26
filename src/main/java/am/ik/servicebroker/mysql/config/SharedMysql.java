package am.ik.servicebroker.mysql.config;

import com.zaxxer.hikari.HikariConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SharedMysql {
    private final HikariConfig hikariConfig;

    public SharedMysql(HikariConfig hikariConfig) {
        this.hikariConfig = hikariConfig;
    }

    public Credentials credentials(String name, String username, String password) {
        String uri = this.hikariConfig.getJdbcUrl().replace("jdbc:", "");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri);
        UriComponents components = builder.build();
        int port = components.getPort() == -1 ? 3306 : components.getPort();
        Credentials credentials = new Credentials();
        credentials.setHostname(components.getHost());
        credentials.setPort(port);
        credentials.setName(name);
        credentials.setUsername(username);
        credentials.setPassword(password);
        credentials.setUri(builder.cloneBuilder()
                .userInfo(username + ":" + password)
                .port(port)
                .replacePath(name)
                .queryParam("reconnect", "true")
                .build()
                .toUriString());
        credentials.setJdbcUrl("jdbc:" + builder.cloneBuilder()
                .port(port)
                .replacePath(name)
                .queryParam("user", username)
                .queryParam("password", password)
                .build()
                .toUriString());
        return credentials;
    }

    public static class Credentials {
        public String hostname;
        public int port;
        public String name;
        public String username;
        public String password;
        public String uri;
        public String jdbcUrl;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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
            this.password = password;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        @Override
        public String toString() {
            return "Credentials{" +
                    "hostname='" + hostname + '\'' +
                    ", port=" + port +
                    ", name='" + name + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", uri='" + uri + '\'' +
                    ", jdbcUrl='" + jdbcUrl + '\'' +
                    '}';
        }
    }
}
