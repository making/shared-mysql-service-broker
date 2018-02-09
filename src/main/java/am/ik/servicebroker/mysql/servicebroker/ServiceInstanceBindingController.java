package am.ik.servicebroker.mysql.servicebroker;

import am.ik.servicebroker.mysql.config.SharedMysql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v2/service_instances/{instanceId}/service_bindings/{bindingId}")
public class ServiceInstanceBindingController {
    private static final Logger log = LoggerFactory.getLogger(ServiceInstanceBindingController.class);
    private final JdbcTemplate jdbcTemplate;
    private final SharedMysql sharedMysql;


    public ServiceInstanceBindingController(JdbcTemplate jdbcTemplate, SharedMysql sharedMysql) {
        this.jdbcTemplate = jdbcTemplate;
        this.sharedMysql = sharedMysql;
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> bind(@PathVariable("instanceId") String instanceId,
                                                    @PathVariable("bindingId") String bindingId) {
        log.info("bind instanceId={}, bindingId={}", instanceId, bindingId);
        Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_instance_binding WHERE instance_id = ? AND binding_id = ?",
                Integer.class, instanceId, bindingId);
        Map<String, Object> body = new HashMap<>();

        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        String name = this.jdbcTemplate.queryForObject("SELECT db_name FROM service_instance WHERE instance_id = ?",
                String.class, instanceId);
        String username = UUID.randomUUID().toString().replace("-", "").substring(16);
        String password = UUID.randomUUID().toString().replace("-", "");

        this.jdbcTemplate.update("INSERT INTO service_instance_binding(instance_id, binding_id, username) VALUES(?, ?, ?)",
                instanceId, bindingId, username);

        this.jdbcTemplate.execute("CREATE USER '" + username + "' IDENTIFIED BY '" + password + "'");
        this.jdbcTemplate.execute("GRANT ALL PRIVILEGES ON " + name + ".* TO '" + username + "'@'%'");
        this.jdbcTemplate.execute("FLUSH PRIVILEGES");

        SharedMysql.Credentials credentials = this.sharedMysql.credentials(name, username, password);
        body.put("credentials", credentials);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> unbind(@PathVariable("instanceId") String instanceId,
                                                      @PathVariable("bindingId") String bindingId) {
        log.info("unbind instanceId={}, bindingId={}", instanceId, bindingId);
        Map<String, Object> body = new HashMap<>();
        Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_instance_binding WHERE instance_id = ? AND binding_id = ?",
                Integer.class, instanceId, bindingId);
        if (count != null && count == 0) {
            return ResponseEntity.status(HttpStatus.GONE).body(body);
        }
        String username = this.jdbcTemplate.queryForObject("SELECT username FROM service_instance_binding WHERE instance_id = ? AND binding_id = ?",
                String.class, instanceId, bindingId);
        this.jdbcTemplate.update("DELETE FROM service_instance_binding WHERE instance_id = ? AND binding_id = ?",
                instanceId, bindingId);
        jdbcTemplate.execute("DROP USER '" + username + "'");
        return ResponseEntity.ok(body);
    }
}
