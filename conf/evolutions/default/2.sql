
# --- !Ups

CREATE TABLE game (
	team1_id INT NOT NULL, 
	team2_id INT NOT NULL,
	time VARCHAR(8) NOT NULL,
	game_date DATE,
	PRIMARY KEY(team1_id, team2_id));


# --- !Downs

DROP TABLE if exists game;

