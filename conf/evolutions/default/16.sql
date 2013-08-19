# --- !Ups

CREATE TABLE video_category ( 
	id INT NOT NULL AUTO_INCREMENT,
 	name VARCHAR(30) NOT NULL,
	PRIMARY KEY(id));

CREATE TABLE video (
	id INT NOT NULL AUTO_INCREMENT,
	video_category_id INT NOT NULL,  
	video_player_id INT NOT NULL,
	link VARCHAR(255) NOT NULL,
	description VARCHAR(255) NOT NULL, 
	title VARCHAR(255) NOT NULL, 
	date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(id));

# --- !Downs
 
DROP TABLE if exists video_category;
