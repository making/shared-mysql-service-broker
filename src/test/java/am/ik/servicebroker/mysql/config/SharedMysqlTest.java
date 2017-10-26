package am.ik.servicebroker.mysql.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedMysqlTest {
    @Test
    public void credentials() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/mysql_service_broker");
        SharedMysql sharedMysql = new SharedMysql(hikariConfig);
        SharedMysql.Credentials credentials = sharedMysql.credentials("abc", "uuu", "ppp");
        assertThat(credentials.getHostname()).isEqualTo("localhost");
        assertThat(credentials.getPort()).isEqualTo(3306);
        assertThat(credentials.getUsername()).isEqualTo("uuu");
        assertThat(credentials.getPassword()).isEqualTo("ppp");
        assertThat(credentials.getUri()).isEqualTo("mysql://uuu:ppp@localhost:3306/abc?reconnect=true");
        assertThat(credentials.getJdbcUrl()).isEqualTo("jdbc:mysql://localhost:3306/abc?user=uuu&password=ppp");
    }

    @Test
    public void credentialsNonPort() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost/mysql_service_broker");
        SharedMysql sharedMysql = new SharedMysql(hikariConfig);
        SharedMysql.Credentials credentials = sharedMysql.credentials("abc", "uuu", "ppp");
        assertThat(credentials.getHostname()).isEqualTo("localhost");
        assertThat(credentials.getPort()).isEqualTo(3306);
        assertThat(credentials.getUsername()).isEqualTo("uuu");
        assertThat(credentials.getPassword()).isEqualTo("ppp");
        assertThat(credentials.getUri()).isEqualTo("mysql://uuu:ppp@localhost:3306/abc?reconnect=true");
        assertThat(credentials.getJdbcUrl()).isEqualTo("jdbc:mysql://localhost:3306/abc?user=uuu&password=ppp");
    }

    @Test
    public void credentialsNonDefaultPort() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:9999/mysql_service_broker");
        SharedMysql sharedMysql = new SharedMysql(hikariConfig);
        SharedMysql.Credentials credentials = sharedMysql.credentials("abc", "uuu", "ppp");
        assertThat(credentials.getHostname()).isEqualTo("localhost");
        assertThat(credentials.getPort()).isEqualTo(9999);
        assertThat(credentials.getUsername()).isEqualTo("uuu");
        assertThat(credentials.getPassword()).isEqualTo("ppp");
        assertThat(credentials.getUri()).isEqualTo("mysql://uuu:ppp@localhost:9999/abc?reconnect=true");
        assertThat(credentials.getJdbcUrl()).isEqualTo("jdbc:mysql://localhost:9999/abc?user=uuu&password=ppp");
    }
}