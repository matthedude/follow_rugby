# Widgets schema
 
# --- !Ups

CREATE TABLE widget (
	id BIGINT SIGNED NOT NULL, 
	twitter_account VARCHAR(30) NOT NULL, 
	url VARCHAR(30) NOT NULL, 
	PRIMARY KEY(id));

CREATE TABLE category (
	id INT NOT NULL AUTO_INCREMENT,
 	name VARCHAR(30) NOT NULL, 
	widget_id BIGINT SIGNED, 
	PRIMARY KEY(id));

CREATE TABLE team (
	id INT NOT NULL AUTO_INCREMENT, 
	name VARCHAR(50) NOT NULL, 
	widget_id BIGINT SIGNED NOT NULL, 
	category_id INT NOT NULL, 
	twitter_name VARCHAR(30), 
	PRIMARY KEY(id));

CREATE TABLE member (
	id INT NOT NULL AUTO_INCREMENT, 
	name VARCHAR(50) NOT NULL, 
	twitter_name VARCHAR(30), 
	PRIMARY KEY(id));

CREATE TABLE team_member (
	team_id INT NOT NULL,
	member_id INT NOT NULL,
	PRIMARY KEY(team_id, member_id));





# --- !Downs
 
DROP TABLE if exists widget;

DROP TABLE if exists category; 

DROP TABLE if exists team;

DROP TABLE if exists member;

DROP TABLE if exists team_member;


