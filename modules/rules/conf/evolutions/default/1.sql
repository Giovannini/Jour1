# --- !Ups

CREATE TABLE rules (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	label VARCHAR(255) NOT NULL,
	param TEXT,
    precond TEXT,
	content TEXT,
	PRIMARY KEY(id)
)
ENGINE=MYISAM;

# --- !Downs

DROP TABLE rules;