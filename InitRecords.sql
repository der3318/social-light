INSERT INTO "users" (account, password, name, type, location, motto, intro, url_avatar) 
VALUES ("account1", "password1", "測試使用者1", "癌症病人", "台北", "座右銘1", "自我介紹1", "https://i.imgur.com/PLjbhKB.png");
INSERT INTO "users" (account, password, name, type, location, motto, intro, url_avatar) 
VALUES ("account2", "password2", "測試使用者2", "肺炎病人", "台南", "座右銘2", "自我介紹2", "https://i.imgur.com/PLjbhKB.png");
INSERT INTO "users" (account, password, name, type, location, motto, intro, url_avatar) 
VALUES ("account3", "password3", "測試使用者3", "癱瘓病人", "台中", "座右銘3", "自我介紹3", "https://i.imgur.com/PLjbhKB.png");

INSERT INTO "boards" (name) VALUES ("分類看版一");
INSERT INTO "boards" (name) VALUES ("分類看版二");

INSERT INTO "posts" (id_user, id_board, title, content, url_avatar)
VALUES (1, 1, "文章1", "內文1", "https://i.imgur.com/o55SVHK.png");
INSERT INTO "posts" (id_user, id_board, title, content, url_avatar)
VALUES (1, 1, "文章2", "內文2", "https://i.imgur.com/o55SVHK.png");
INSERT INTO "posts" (id_user, id_board, title, content, url_avatar)
VALUES (2, 1, "文章3", "內文3", "https://i.imgur.com/o55SVHK.png");
INSERT INTO "posts" (id_user, id_board, title, content, url_avatar)
VALUES (2, 2, "文章4", "內文4", "https://i.imgur.com/o55SVHK.png");
INSERT INTO "posts" (id_user, id_board, title, content, url_avatar)
VALUES (3, 2, "文章5", "內文5", "https://i.imgur.com/o55SVHK.png");
INSERT INTO "posts" (id_user, id_board, title, content, url_avatar)
VALUES (3, 2, "文章6", "內文6", "https://i.imgur.com/o55SVHK.png");

INSERT INTO "comments" (id_user, id_post, content) VALUES (1, 1, "留言1");
INSERT INTO "comments" (id_user, id_post, content) VALUES (1, 2, "留言2");
INSERT INTO "comments" (id_user, id_post, content) VALUES (1, 3, "留言3");
INSERT INTO "comments" (id_user, id_post, content) VALUES (2, 4, "留言4");
INSERT INTO "comments" (id_user, id_post, content) VALUES (2, 5, "留言5");
INSERT INTO "comments" (id_user, id_post, content) VALUES (2, 6, "留言6");
INSERT INTO "comments" (id_user, id_post, content) VALUES (3, 1, "留言7");
INSERT INTO "comments" (id_user, id_post, content) VALUES (3, 3, "留言8");
INSERT INTO "comments" (id_user, id_post, content) VALUES (3, 5, "留言9");

INSERT INTO "chatrooms" (id_user, id_user_target, name, url_avatar)
VALUES (1, 2, "測試使用者2", "https://i.imgur.com/PLjbhKB.png");
INSERT INTO "chatrooms" (id_user, id_user_target, name, url_avatar)
VALUES (2, 1, "測試使用者1", "https://i.imgur.com/PLjbhKB.png");

INSERT INTO "messages" (id_chatroom, status, content) VALUES (1, 2, "訊息測試");
INSERT INTO "messages" (id_chatroom, status, content) VALUES (2, 0, "訊息測試");
