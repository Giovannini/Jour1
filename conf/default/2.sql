# --- !Ups

CREATE TABLE preconditions (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	label VARCHAR(255) NOT NULL,
	parameters TEXT,
  subconditions TEXT
	PRIMARY KEY(id)
)
ENGINE=MYISAM;

# --- !Downs

DROP TABLE preconditions;