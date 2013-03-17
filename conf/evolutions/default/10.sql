# --- !Ups

create table user (
  email                     varchar(255) not null,
  name                      varchar(255) not null,
  password                  varchar(255) not null, 
  PRIMARY KEY(email)
);

# --- !Downs

drop table if exists user;
