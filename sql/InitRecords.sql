INSERT INTO users (account, password, name, type, location, motto, intro, url_avatar)
VALUES ("account1", "password1", "測試使用者一號", "癌症病人", "台北", "座右銘呢", "首先自我介紹", "user1.png");
INSERT INTO users (account, password, name, type, location, motto, intro, url_avatar)
VALUES ("account2", "password2", "測試使用者二號", "肺炎病人", "台南", "有座右銘", "接著自我介紹", "user2.png");
INSERT INTO users (account, password, name, type, location, motto, intro, url_avatar)
VALUES ("account3", "password3", "測試使用者三號", "癱瘓病人", "台中", "沒座右銘", "最後自我介紹", "user3.png");

INSERT INTO boards (name) VALUES ("分類看板一");
INSERT INTO boards (name) VALUES ("分類看板二");

INSERT INTO posts (id_user, id_board, title, content, url_avatar)
VALUES (1, -1, "文章標題一", "內文，標點，第一篇", "post1.jpg");
INSERT INTO posts (id_user, id_board, title, content, url_avatar)
VALUES (1, 1, "文章標題二", "內文，標點，第二篇", "post2.jpg");
INSERT INTO posts (id_user, id_board, title, content, url_avatar)
VALUES (2, 1, "文章標題三", "內文，標點，第三篇", "post3.jpg");
INSERT INTO posts (id_user, id_board, title, content, url_avatar)
VALUES (2, -1, "文章標題四", "內文，標點，第四篇", "post4.jpg");
INSERT INTO posts (id_user, id_board, title, content, url_avatar)
VALUES (3, 2, "文章標題五", "內文，標點，第五篇", "post5.jpg");
INSERT INTO posts (id_user, id_board, title, content, url_avatar)
VALUES (3, 2, "文章標題六", "內文，標點，第六篇", "post6.jpg");

INSERT INTO comments (id_user, id_post, content) VALUES (1, 1, "文章留言一");
INSERT INTO comments (id_user, id_post, content) VALUES (1, 2, "文章留言二");
INSERT INTO comments (id_user, id_post, content) VALUES (1, 3, "文章留言三");
INSERT INTO comments (id_user, id_post, content) VALUES (2, 4, "文章留言四");
INSERT INTO comments (id_user, id_post, content) VALUES (2, 5, "文章留言五");
INSERT INTO comments (id_user, id_post, content) VALUES (2, 6, "文章留言六");
INSERT INTO comments (id_user, id_post, content) VALUES (3, 2, "文章留言七");
INSERT INTO comments (id_user, id_post, content) VALUES (3, 4, "文章留言八");
INSERT INTO comments (id_user, id_post, content) VALUES (3, 6, "文章留言九");

INSERT INTO chatrooms (id_user, id_user_target, name, url_avatar)
VALUES (1, 2, "測試使用者二號", "user2.png");
INSERT INTO chatrooms (id_user, id_user_target, name, url_avatar)
VALUES (2, 1, "測試使用者一號", "user1.png");

INSERT INTO messages (id_chatroom, status, content) VALUES (1, 2, "訊息測試");
INSERT INTO messages (id_chatroom, status, content) VALUES (2, 0, "訊息測試");
