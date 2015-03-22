# --- !Ups

CREATE TABLE needs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  label VARCHAR(20) NOT NULL,
  property VARCHAR(30) NOT NULL,
  priority INT NOT NULL,
  consequencesSteps VARCHAR(255) NOT NULL,
  meansOfSatisfaction VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
)
  ENGINE=MYISAM;

# --- !Downs

DROP TABLE needs;