
# --- !Ups

CREATE TABLE game (
	team1_id INT NOT NULL, 
	team2_id INT NOT NULL,
	competition_id INT NOT NULL,
	time VARCHAR(20) NOT NULL,
	game_date DATE,
	widget_id BIGINT SIGNED NOT NULL,
	pos INT NOT NULL,
	PRIMARY KEY(team1_id, team2_id));


# --- !Downs

DROP TABLE if exists game;

