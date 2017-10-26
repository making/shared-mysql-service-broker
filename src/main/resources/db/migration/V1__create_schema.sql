CREATE TABLE IF NOT EXISTS service_instance (
  `instance_id` VARCHAR(36) NOT NULL,
  `db_name`     VARCHAR(36) NOT NULL,
  `created_at`  TIMESTAMP   NOT NULL DEFAULT now(),
  PRIMARY KEY (`instance_id`)
)
  ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS service_instance_binding (
  `instance_id` VARCHAR(36) NOT NULL,
  `binding_id`  VARCHAR(36) NOT NULL,
  `username`    VARCHAR(36) NOT NULL,
  `created_at`  TIMESTAMP   NOT NULL DEFAULT now(),
  PRIMARY KEY (`instance_id`, `binding_id`),
  CONSTRAINT FOREIGN KEY (instance_id) REFERENCES service_instance (instance_id)
    ON DELETE CASCADE
)
  ENGINE InnoDB;