# --- !Ups

CREATE TABLE comp_entry ( 
	name VARCHAR(50) NOT NULL, 
	email VARCHAR(100) NOT NULL,
	phone VARCHAR(100) NOT NULL, 
	players VARCHAR(200) NOT NULL, 
	PRIMARY KEY(email));

# --- !Downs
 
DROP TABLE if exists comp_entry;
