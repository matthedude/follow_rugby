# --- !Ups

CREATE TABLE comp_player (
	id INT NOT NULL AUTO_INCREMENT, 
	name VARCHAR(50) NOT NULL, 
	position VARCHAR(30) NOT NULL, 
	PRIMARY KEY(id));

# --- !Downs
 
DROP TABLE if exists comp_player;
