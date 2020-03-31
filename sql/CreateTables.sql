drop table IF EXISTS users;
create TABLE users (
	"id"				INTEGER PRIMARY KEY AUTOINCREMENT,
	"account"			VARCHAR(255),
	"password"			VARCHAR(255),
	"name"				VARCHAR(255),
	"type"				VARCHAR(255),
	"location"			VARCHAR(255),
	"motto"				VARCHAR(255),
	"intro"				VARCHAR(2047),
	"url_avatar"		VARCHAR(255),
	"ts_create"			DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table IF EXISTS boards;
create TABLE boards (
	"id"				INTEGER PRIMARY KEY AUTOINCREMENT,
	"name"				VARCHAR(255),
	"ts_create"			DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table IF EXISTS posts;
create TABLE posts (
	"id"				INTEGER PRIMARY KEY AUTOINCREMENT,
	"id_user"			INTEGER,
	"id_board"			INTEGER,
	"title"				VARCHAR(255),
	"content"			VARCHAR(2047),
	"url_avatar"		VARCHAR(255),
	"ts_create"			DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table IF EXISTS comments;
create TABLE comments (
	"id"				INTEGER PRIMARY KEY AUTOINCREMENT,
	"id_user"			INTEGER,
	"id_post"			INTEGER,
	"content"			VARCHAR(2047),
	"ts_create"			DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table IF EXISTS chatrooms;
create TABLE chatrooms (
	"id"				INTEGER PRIMARY KEY AUTOINCREMENT,
	"id_user"			INTEGER,
	"id_user_target"	INTEGER,
	"name"				VARCHAR(255),
	"url_avatar"		VARCHAR(255),
	"ts_create"			DATETIME DEFAULT CURRENT_TIMESTAMP
);

drop table IF EXISTS messages;
create TABLE messages (
	"id"				INTEGER PRIMARY KEY AUTOINCREMENT,
	"id_chatroom"		INTEGER,
	"status"			INTEGER,
	"content"			VARCHAR(255),
	"ts_create"			DATETIME DEFAULT CURRENT_TIMESTAMP
);
