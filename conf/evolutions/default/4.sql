# --- !Ups

CREATE TABLE properties (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  label VARCHAR(20) NOT NULL,
  defaultValue DOUBLE NOT NULL,
  PRIMARY KEY(id)
)
  ENGINE=MYISAM;

# --- !Downs

DROP TABLE properties;