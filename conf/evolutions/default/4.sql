# --- !Ups

CREATE TABLE properties (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  label VARCHAR(20) NOT NULL,
  type VARCHAR(10) NOT NULL,
  defaultValue VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
)
  ENGINE=MYISAM;

# --- !Downs

DROP TABLE properties;