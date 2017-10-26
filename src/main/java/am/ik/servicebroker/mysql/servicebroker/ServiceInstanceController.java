package am.ik.servicebroker.mysql.servicebroker;

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
@RequestMapping("/v2/service_instances/{instanceId}")
public class ServiceInstanceController {
    private static final Logger log = LoggerFactory.getLogger(ServiceInstanceController.class);
    private final JdbcTemplate jdbcTemplate;


    public ServiceInstanceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> provisioning(@PathVariable("instanceId") String instanceId) {
        log.info("Provisioning instanceId={}", instanceId);
        Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_instance WHERE instance_id = ?",
                Integer.class, instanceId);
        Map<String, Object> body = new HashMap<>();
        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        String dbName = "cf_" + UUID.randomUUID().toString().replace("-", "");
        this.jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + dbName);
        this.jdbcTemplate.update("INSERT INTO service_instance(instance_id, db_name) VALUES(?, ?)",
                instanceId, dbName);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PatchMapping
    public ResponseEntity<Map<String, Object>> update(@PathVariable("instanceId") String instanceId) {
        Map<String, Object> body = new HashMap<>();
        return ResponseEntity.ok(body);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> deprovisioning(@PathVariable("instanceId") String instanceId) {
        log.info("Deprovisioning instanceId={}", instanceId);
        Map<String, Object> body = new HashMap<>();
        Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_instance WHERE instance_id = ?",
                Integer.class, instanceId);
        if (count != null && count == 0) {
            return ResponseEntity.status(HttpStatus.GONE).body(body);
        }
        String dbName = this.jdbcTemplate.queryForObject("SELECT db_name FROM service_instance WHERE instance_id = ?",
                String.class, instanceId);
        this.jdbcTemplate.update("DELETE FROM service_instance WHERE instance_id = ?", instanceId);
        this.jdbcTemplate.execute("DROP DATABASE IF EXISTS " + dbName);
        return ResponseEntity.ok(body);
    }
}
