# --- !Ups

create table competition (
  id INT NOT NULL AUTO_INCREMENT,
  name varchar(255) not null,
  pos INT,
  PRIMARY KEY(id)
);

# --- !Downs

drop table if exists competition;
