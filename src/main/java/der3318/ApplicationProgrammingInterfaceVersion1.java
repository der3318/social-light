package der3318;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlStatements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jooby.Jooby;
import io.jooby.MediaType;
import io.jooby.hikari.HikariModule;
import io.jooby.jdbi.JdbiModule;
import io.jooby.json.JacksonModule;

public class ApplicationProgrammingInterfaceVersion1 extends Jooby {
    {
        /* database interface */
        install(new HikariModule());
        install(new JdbiModule());
        Jdbi jdbi = require(Jdbi.class);
        jdbi.getConfig(SqlStatements.class).setUnusedBindingAllowed(true);

        /* json parser */
        install(new JacksonModule());
        ObjectMapper mapper = require(ObjectMapper.class);

        /* specify response type as json */
        decorator(next -> ctx -> {
            ctx.setResponseType(MediaType.json, StandardCharsets.UTF_8);
            return mapper.writeValueAsString(next.apply(ctx));
        });

        /* json to hash map */
        TypeReference<HashMap<String, Object>> mapRef = new TypeReference<HashMap<String, Object>>() {
        };

        /* tokens for authentication */
        HashMap<Integer, String> tokens = new HashMap<>();

        /* time pattern */
        Pattern timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");

        /* [api] admin database reset */
        post("/admin/database/reset", ctx -> {
            jdbi.useHandle(h -> {
                h.createScript(new String(Files.readAllBytes(Paths.get(new File("sql/CreateTables.sql").getAbsolutePath())), StandardCharsets.UTF_8)).execute();
                h.createScript(new String(Files.readAllBytes(Paths.get(new File("sql/InitRecords.sql").getAbsolutePath())), StandardCharsets.UTF_8)).execute();
            });
            return new JsonResponse(0).body();
        });

        /* [api] info */
        post("/info", ctx -> {
            JsonResponse rsp = new JsonResponse(0);
            rsp.set("developer", "der3318").set("date", "2020 March 8th");
            List<Map<String, Object>> tables = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
                return h.createQuery(sql).mapToMap().list();
            });
            return rsp.set("tables", tables).body();
        });

        /* [api] login */
        post("/login", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            String account = (String) req.getOrDefault("account", "");
            String password = (String) req.getOrDefault("password", "");
            Optional<Map<String, Object>> record = jdbi.withHandle(h -> {
                String sql = "SELECT id, password FROM users WHERE account = :account LIMIT 1";
                return h.createQuery(sql).bind("account", account).mapToMap().findFirst();
            });
            if (!record.isPresent()) {
                return rsp.set("code", -2).body();
            }
            Map<String, Object> user = record.get();
            if (!user.get("password").equals(password)) {
                return rsp.set("code", -1).body();
            }
            Integer id = (Integer) user.get("id");
            tokens.put(id, sha256(String.format("%d", new Random().nextInt())));
            return rsp.set("id", id).set("token", tokens.get(id)).body();
        });

        /* [api] get user info */
        post("/user", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Optional<Map<String, Object>> record = jdbi.withHandle(h -> {
                String sql = "SELECT account, name, type, location, motto, intro, url_avatar FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            if (!record.isPresent()) {
                return rsp.set("code", -1).body();
            }
            return rsp.set(record.get()).body();
        });

        /* [api] update user info */
        post("/user/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> record = jdbi.withHandle(h -> {
                String sql = "SELECT password, name, type, location, motto, intro, url_avatar FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            if (!record.isPresent()) {
                return rsp.set("code", -1).body();
            }
            if (!tokens.containsKey(id) || !tokens.get(id).equals(token)) {
                return rsp.set("code", -2).body();
            }
            Map<String, Object> user = record.get();
            user.putAll(req);
            jdbi.useHandle(h -> {
                String sql = "UPDATE users SET password = :password, name = :name, type = :type, location = :location, motto = :motto, intro = :intro, url_avatar = :url_avatar WHERE id = :id";
                h.createUpdate(sql).bindMap(user).execute();
            });
            return rsp.body();
        });

        /* [api] get list of boards */
        post("/boards", ctx -> {
            JsonResponse rsp = new JsonResponse(0);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT id, name FROM boards ORDER BY id";
                return h.createQuery(sql).mapToMap().list();
            });
            return rsp.set("boards", records).body();
        });

        /* [api] get posts with filters */
        post("/posts", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer policy = (Integer) req.getOrDefault("policy", Integer.MIN_VALUE);
            if (policy < 0 || policy > 2) {
                return rsp.set("code", -1).body();
            }
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String params = req.containsKey("id_board") ? "id_board = :id_board" : "id_board >= 0";
                params = req.containsKey("id_user") ? params + " AND id_user = :id_user" : params;
                params = req.containsKey("keyword") ? params + " AND title LIKE '%' || :keyword || '%'" : params;
                params = req.containsKey("ts") ? params + " AND ts_create >= :ts" : params;
                params = policy == 1 ? params + " ORDER BY ts_create DESC" : policy == 2 ? params + " ORDER by title" : params;
                String sql = String.format("SELECT id, id_user, id_board, title, content, url_avatar, ts_create FROM posts WHERE %s", params);
                return h.createQuery(sql).bindMap(req).mapToMap().list();
            });
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            return rsp.set("posts", records).body();
        });

        /* [api] publish or update post */
        post("/post/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Integer userID = (Integer) req.getOrDefault("id_user", Integer.MIN_VALUE);
            Integer boardID = (Integer) req.getOrDefault("id_board", Integer.MIN_VALUE);
            String title = (String) req.getOrDefault("title", "");
            String content = (String) req.getOrDefault("content", "");
            String avatar = (String) req.getOrDefault("url_avatar", "");
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", userID).mapToMap().findFirst();
            });
            if (!userRecord.isPresent()) {
                return rsp.set("code", -1).body();
            }
            Optional<Map<String, Object>> boardRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM boards WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", boardID).mapToMap().findFirst();
            });
            if (boardID != -1 && !boardRecord.isPresent()) {
                return rsp.set("code", -2).body();
            }
            if (title.trim().isEmpty() || content.trim().isEmpty() || avatar.trim().isEmpty()) {
                return rsp.set("code", -3).body();
            }
            if (!tokens.containsKey(userID) || !tokens.get(userID).equals(token)) {
                return rsp.set("code", -4).body();
            }
            Optional<Map<String, Object>> postRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM posts WHERE id = :id AND id_user = :user_id LIMIT 1";
                return h.createQuery(sql).bind("id", id).bind("user_id", userID).mapToMap().findFirst();
            });
            Optional<Integer> newRecord = jdbi.withHandle(h -> {
                String sql = "INSERT INTO posts (id_user, id_board, title, content, url_avatar) VALUES (:id_user, :id_board, :title, :content, :url_avatar)";
                if (postRecord.isPresent()) {
                    sql = "UPDATE posts SET id_user = :id_user, id_board = :id_board, title = :title, content = :content, url_avatar = :url_avatar WHERE id = :id";
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if (!postRecord.isPresent() && newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
        });

        /* [api] get post info and related comments */
        post("/post", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Optional<Map<String, Object>> record = jdbi.withHandle(h -> {
                String sql = "SELECT id_user, id_board, title, content, url_avatar, ts_create FROM posts WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            if (!record.isPresent()) {
                return rsp.set("code", -1).body();
            }
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT id, id_user, content, ts_create FROM comments WHERE id_post = :id ORDER BY ts_create ASC";
                return h.createQuery(sql).bind("id", id).mapToMap().list();
            });
            Map<String, Object> post = record.get();
            finalizeDatetime(timePattern, post, "ts_create");
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            return rsp.set(post).set("comments", records).body();
        });

        /* publish or update a comment */
        post("/comment/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Integer userID = (Integer) req.getOrDefault("id_user", Integer.MIN_VALUE);
            Integer postID = (Integer) req.getOrDefault("id_post", Integer.MIN_VALUE);
            String content = (String) req.getOrDefault("content", "");
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", userID).mapToMap().findFirst();
            });
            if (!userRecord.isPresent()) {
                return rsp.set("code", -1).body();
            }
            Optional<Map<String, Object>> postRecord = jdbi.withHandle(h -> {
                String sql = "SELECT title FROM posts WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", postID).mapToMap().findFirst();
            });
            if (!postRecord.isPresent()) {
                return rsp.set("code", -2).body();
            }
            if (content.isEmpty()) {
                return rsp.set("code", -3).body();
            }
            if (!tokens.containsKey(userID) || !tokens.get(userID).equals(token)) {
                return rsp.set("code", -4).body();
            }
            Optional<Map<String, Object>> commentRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM comments WHERE id = :id AND id_user = :user_id LIMIT 1";
                return h.createQuery(sql).bind("id", id).bind("user_id", userID).mapToMap().findFirst();
            });
            Optional<Integer> newRecord = jdbi.withHandle(h -> {
                String sql = "INSERT INTO comments (id_user, id_post, content) VALUES (:id_user, :id_post, :content)";
                if (commentRecord.isPresent()) {
                    sql = "UPDATE comments SET id_user = :id_user, id_post = :id_post, content = :content WHERE id = :id";
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if (!commentRecord.isPresent() && newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
        });

        /* [api] get chatrooms of a user */
        post("/chatrooms", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            if (!userRecord.isPresent()) {
                return rsp.set("code", -1).body();
            }
            if (!tokens.containsKey(id) || !tokens.get(id).equals(token)) {
                return rsp.set("code", -2).body();
            }
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                StringBuilder sql = new StringBuilder()
                        .append("SELECT chatrooms.id AS id, id_user_target, name, url_avatar, MAX(messages.ts_create) AS lastmsg_ts ")
                        .append("FROM chatrooms LEFT JOIN messages ON chatrooms.id = messages.id_chatroom ")
                        .append("GROUP BY chatrooms.id, id_user_target, name, url_avatar HAVING id_user = :id ORDER BY lastmsg_ts DESC");
                return h.createQuery(sql.toString()).bind("id", id).mapToMap().list();
            });
            for (Map<String, Object> r : records) {
                Optional<Map<String, Object>> msgRecord = jdbi.withHandle(h -> {
                    String sql = "SELECT status AS lastmsg_status, content AS lastmsg_content FROM messages WHERE id_chatroom = :id AND ts_create = :lastmsg_ts LIMIT 1";
                    return h.createQuery(sql).bindMap(r).mapToMap().findFirst();
                });
                r.putAll(msgRecord.get());
                finalizeDatetime(timePattern, r, "lastmsg_ts");
            }
            return rsp.set("chatrooms", records).body();
        });

        /* [api] update a chatroom */
        post("/chatroom/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Integer userID = (Integer) req.getOrDefault("id_user", Integer.MIN_VALUE);
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> chatroomRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name, url_avatar FROM chatrooms WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            if (!chatroomRecord.isPresent()) {
                return rsp.set("code", -1).body();
            }
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", userID).mapToMap().findFirst();
            });
            if (!userRecord.isPresent()) {
                return rsp.set("code", -2).body();
            }
            if (!tokens.containsKey(userID) || !tokens.get(userID).equals(token)) {
                return rsp.set("code", -3).body();
            }
            Map<String, Object> chatroom = chatroomRecord.get();
            chatroom.putAll(req);
            jdbi.useHandle(h -> {
                String sql = "UPDATE chatrooms SET name = :name, url_avatar = :url_avatar WHERE id = :id";
                h.createUpdate(sql).bindMap(chatroom).execute();
            });
            return rsp.body();
        });

        /* [api] get message between users */
        post("/messages", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Integer targetID = (Integer) req.getOrDefault("id_user_target", Integer.MIN_VALUE);
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            if (!userRecord.isPresent()) {
                return rsp.set("code", -1).body();
            }
            Optional<Map<String, Object>> chatroomRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id AS id_chatroom, name, url_avatar FROM chatrooms WHERE id_user = :id_user AND id_user_target = :id_user_target LIMIT 1";
                return h.createQuery(sql).bind("id_user", id).bind("id_user_target", targetID).mapToMap().findFirst();
            });
            if (!chatroomRecord.isPresent()) {
                return rsp.set("code", -2).body();
            }
            if (!tokens.containsKey(id) || !tokens.get(id).equals(token)) {
                return rsp.set("code", -3).body();
            }
            Map<String, Object> chatroom = chatroomRecord.get();
            rsp.set(chatroom);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT status, content, ts_create FROM messages WHERE id_chatroom = :id_chatroom ORDER BY ts_create DESC LIMIT 100";
                return h.createQuery(sql).bind("id_chatroom", chatroom.get("id_chatroom")).mapToMap().list();
            });
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            return rsp.set("messages", records).body();
        });

        /* [api] send message */
        post("/message/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Integer targetID = (Integer) req.getOrDefault("id_user_target", Integer.MIN_VALUE);
            String content = (String) req.getOrDefault("content", "");
            String token = (String) req.getOrDefault("token", "");
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name, url_avatar FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            Optional<Map<String, Object>> userTargetRecord = jdbi.withHandle(h -> {
                String sql = "SELECT name, url_avatar FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", targetID).mapToMap().findFirst();
            });
            if (!userRecord.isPresent() || !userTargetRecord.isPresent()) {
                return rsp.set("code", -1).body();
            }
            Map<String, Object> user = userRecord.get(), userTarget = userTargetRecord.get();
            if (!tokens.containsKey(id) || !tokens.get(id).equals(token)) {
                return rsp.set("code", -2).body();
            }
            Integer chatroomID = -1, chatroomTargetID = -1;
            synchronized (this) {
                Optional<Map<String, Object>> chatroomRecord = jdbi.withHandle(h -> {
                    String sql = "SELECT id AS id_chatroom, name, url_avatar FROM chatrooms WHERE id_user = :id_user AND id_user_target = :id_user_target LIMIT 1";
                    return h.createQuery(sql).bind("id_user", id).bind("id_user_target", targetID).mapToMap().findFirst();
                });
                Optional<Map<String, Object>> chatroomTargetRecord = jdbi.withHandle(h -> {
                    String sql = "SELECT id AS id_chatroom, name, url_avatar FROM chatrooms WHERE id_user = :id_user AND id_user_target = :id_user_target LIMIT 1";
                    return h.createQuery(sql).bind("id_user", targetID).bind("id_user_target", id).mapToMap().findFirst();
                });
                if (chatroomRecord.isPresent() && chatroomTargetRecord.isPresent()) {
                    chatroomID = (Integer) chatroomRecord.get().get("id_chatroom");
                    chatroomTargetID = (Integer) chatroomTargetRecord.get().get("id_chatroom");
                } else {
                    chatroomID = jdbi.withHandle(h -> {
                        String sql = "INSERT INTO chatrooms (id_user, id_user_target, name, url_avatar) VALUES (:id_user, :id_user_target, :name, :url_avatar)";
                        return h.createUpdate(sql)
                                .bind("id_user", id)
                                .bind("id_user_target", targetID)
                                .bind("name", userTarget.get("name"))
                                .bind("url_avatar", userTarget.get("url_avatar"))
                                .executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
                    }).get();
                    chatroomTargetID = jdbi.withHandle(h -> {
                        String sql = "INSERT INTO chatrooms (id_user, id_user_target, name, url_avatar) VALUES (:id_user, :id_user_target, :name, :url_avatar)";
                        return h.createUpdate(sql)
                                .bind("id_user", targetID)
                                .bind("id_user_target", id)
                                .bind("name", user.get("name"))
                                .bind("url_avatar", user.get("url_avatar"))
                                .executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
                    }).get();
                }
            }
            Integer newChatroomID = chatroomID, newChatroomTargetID = chatroomTargetID;
            jdbi.useHandle(h -> {
                String sql = "INSERT INTO messages (id_chatroom, status, content) VALUES (:id_chatroom, :status, :content)";
                h.createUpdate(sql).bind("id_chatroom", newChatroomID).bind("status", 2).bind("content", content).execute();
                h.createUpdate(sql).bind("id_chatroom", newChatroomTargetID).bind("status", 0).bind("content", content).execute();
            });
            Map<String, Object> chatroom = jdbi.withHandle(h -> {
                String sql = "SELECT id AS id_chatroom, name, url_avatar FROM chatrooms WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", newChatroomID).mapToMap().findFirst();
            }).get();
            rsp.set(chatroom);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT status, content, ts_create FROM messages WHERE id_chatroom = :id_chatroom ORDER BY ts_create DESC LIMIT 100";
                return h.createQuery(sql).bind("id_chatroom", chatroom.get("id_chatroom")).mapToMap().list();
            });
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            return rsp.set("messages", records).body();
        });

        /* [api] admin view users */
        post("/admin/users", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            String search = (String) req.getOrDefault("search", "");
            StringBuilder params = new StringBuilder()
                    .append("id = :search OR account LIKE '%' || :search || '%' OR name LIKE '%' || :search || '%' ")
                    .append("OR location LIKE '%' || :search || '%' OR motto LIKE '%' || :search || '%' OR intro LIKE '%' || :search || '%' ")
                    .append("ORDER BY (id = :search) DESC, (account LIKE '%' || :search || '%') DESC, (name LIKE '%' || :search || '%') DESC");
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = String.format("SELECT id, account, password, name, type, location, motto, intro, url_avatar FROM users WHERE %s LIMIT 5", params);
                return h.createQuery(sql).bind("search", search).mapToMap().list();
            });
            Optional<Map<String, Object>> countRecord = jdbi.withHandle(h -> {
                String sql = String.format("SELECT COUNT(*) AS count FROM users WHERE %s", params);
                return h.createQuery(sql).bind("search", search).mapToMap().findFirst();
            });
            return rsp.set("data", records).set("total", countRecord.get().get("count")).body();
        });

        /* [api] admin view boards */
        post("/admin/boards", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            String search = (String) req.getOrDefault("search", "");
            StringBuilder params = new StringBuilder()
                    .append("id = :search OR name LIKE '%' || :search || '%' ")
                    .append("ORDER BY (id = :search) DESC, (name LIKE '%' || :search || '%') DESC");
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = String.format("SELECT id, name FROM boards WHERE %s LIMIT 5", params);
                return h.createQuery(sql).bind("search", search).mapToMap().list();
            });
            Optional<Map<String, Object>> countRecord = jdbi.withHandle(h -> {
                String sql = String.format("SELECT COUNT(*) AS count FROM boards WHERE %s", params);
                return h.createQuery(sql).bind("search", search).mapToMap().findFirst();
            });
            return rsp.set("data", records).set("total", countRecord.get().get("count")).body();
        });

        /* [api] admin view posts */
        post("/admin/posts", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            String search = (String) req.getOrDefault("search", "");
            StringBuilder params = new StringBuilder()
                    .append("id = :search OR id_user = :search OR id_board = :search ")
                    .append("OR title LIKE '%' || :search || '%' OR content LIKE '%' || :search || '%' ")
                    .append("ORDER BY (id = :search) DESC, (id_user = :search) DESC, (id_board = :search) DESC");
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = String.format("SELECT id, id_user, id_board, title, content, url_avatar, ts_create FROM posts WHERE %s LIMIT 5", params);
                return h.createQuery(sql).bind("search", search).mapToMap().list();
            });
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            Optional<Map<String, Object>> countRecord = jdbi.withHandle(h -> {
                String sql = String.format("SELECT COUNT(*) AS count FROM posts WHERE %s", params);
                return h.createQuery(sql).bind("search", search).mapToMap().findFirst();
            });
            return rsp.set("data", records).set("total", countRecord.get().get("count")).body();
        });

        /* [api] admin view comments */
        post("/admin/comments", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            String search = (String) req.getOrDefault("search", "");
            StringBuilder params = new StringBuilder()
                    .append("id = :search OR id_user = :search OR id_post = :search ")
                    .append("OR content LIKE '%' || :search || '%' ")
                    .append("ORDER BY (id = :search) DESC, (id_user = :search) DESC, (id_post = :search) DESC");
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = String.format("SELECT id, id_user, id_post, content, ts_create FROM comments WHERE %s LIMIT 5", params);
                return h.createQuery(sql).bind("search", search).mapToMap().list();
            });
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            Optional<Map<String, Object>> countRecord = jdbi.withHandle(h -> {
                String sql = String.format("SELECT COUNT(*) AS count FROM comments WHERE %s", params);
                return h.createQuery(sql).bind("search", search).mapToMap().findFirst();
            });
            return rsp.set("data", records).set("total", countRecord.get().get("count")).body();
        });

        /* [api] admin update user */
        post("/admin/user/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Optional<Map<String, Object>> userRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM users WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            Optional<Integer> newRecord = jdbi.withHandle(h -> {
                String sql = "INSERT INTO users (account, password, name, type, location, motto, intro, url_avatar) VALUES (:account, :password, :name, :type, :location, :motto, :intro, :url_avatar)";
                if (userRecord.isPresent()) {
                    sql = "UPDATE users SET account = :account, password = :password, name = :name, type = :type, location = :location, motto = :motto, intro = :intro, url_avatar = :url_avatar WHERE id = :id";
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if (!userRecord.isPresent() && newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
        });

        /* [api] admin update board */
        post("/admin/board/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Optional<Map<String, Object>> boardRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM boards WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            Optional<Integer> newRecord = jdbi.withHandle(h -> {
                String sql = "INSERT INTO boards (name) VALUES (:name)";
                if (boardRecord.isPresent()) {
                    sql = "UPDATE boards SET name = :name WHERE id = :id";
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if (!boardRecord.isPresent() && newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
        });

        /* [api] admin update post */
        post("/admin/post/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Optional<Map<String, Object>> postRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM posts WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            Optional<Integer> newRecord = jdbi.withHandle(h -> {
                String sql = "INSERT INTO posts (id_user, id_board, title, content, url_avatar, ts_create) VALUES (:id_user, :id_board, :title, :content, :url_avatar, :ts_create)";
                if (postRecord.isPresent()) {
                    sql = "UPDATE posts SET id_user = :id_user, id_board = :id_board, title = :title, content = :content, url_avatar = :url_avatar, ts_create = :ts_create WHERE id = :id";
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if (!postRecord.isPresent() && newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
        });

        /* [api] admin update comment */
        post("/admin/comment/update", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            Optional<Map<String, Object>> commentRecord = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM comments WHERE id = :id LIMIT 1";
                return h.createQuery(sql).bind("id", id).mapToMap().findFirst();
            });
            Optional<Integer> newRecord = jdbi.withHandle(h -> {
                String sql = "INSERT INTO comments (id_user, id_post, content, ts_create) VALUES (:id_user, :id_post, :content, :ts_create)";
                if (commentRecord.isPresent()) {
                    sql = "UPDATE comments SET id_user = :id_user, id_post = :id_post, content = :content, ts_create = :ts_create WHERE id = :id";
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if (!commentRecord.isPresent() && newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
        });

        /* [api] admin delete user */
        post("/admin/user/delete", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM users WHERE id = :id";
                h.createUpdate(sql).bind("id", id).execute();
            });
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM posts WHERE id_user = :id_user";
                h.createUpdate(sql).bind("id_user", id).execute();
            });
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM comments WHERE id_user = :id_user";
                h.createUpdate(sql).bind("id_user", id).execute();
            });
            return rsp.body();
        });

        /* [api] admin delete board */
        post("/admin/board/delete", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            List<String> records = jdbi.withHandle(h -> {
                String sql = "SELECT id FROM posts WHERE id_board = :id_board";
                return h.createQuery(sql).bind("id_board", id).mapTo(String.class).list();
            });
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM boards WHERE id = :id";
                h.createUpdate(sql).bind("id", id).execute();
            });
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM posts WHERE id_board = :id_board";
                h.createUpdate(sql).bind("id_board", id).execute();
            });
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM comments WHERE id_post in (<id_post_list>)";
                h.createUpdate(sql).bindList("id_post_list", records).execute();
            });
            return rsp.body();
        });

        /* [api] admin delete post */
        post("/admin/post/delete", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM posts WHERE id = :id";
                h.createUpdate(sql).bind("id", id).execute();
            });
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM comments WHERE id_post = :id_post";
                h.createUpdate(sql).bind("id_post", id).execute();
            });
            return rsp.body();
        });

        /* [api] admin delete comment */
        post("/admin/comment/delete", ctx -> {
            Map<String, Object> req = mapper.readValue(ctx.body().value(), mapRef);
            JsonResponse rsp = new JsonResponse(0);
            Integer id = (Integer) req.getOrDefault("id", Integer.MIN_VALUE);
            jdbi.useHandle(h -> {
                String sql = "DELETE FROM comments WHERE id = :id";
                h.createUpdate(sql).bind("id", id).execute();
            });
            return rsp.body();
        });

        /* [api] admin users dump */
        post("/admin/users/dump", ctx -> {
            JsonResponse rsp = new JsonResponse(0);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT * FROM users ORDER BY id ASC";
                return h.createQuery(sql).mapToMap().list();
            });
            List<String> columnList = Arrays.asList("id", "account", "password", "name", "type", "location", "motto", "intro", "url_avatar", "ts_create");
            String filename = String.format("public/files/users-%s.csv", System.currentTimeMillis());
            outputCsvFile(records, columnList, filename);
            return rsp.set("url", filename.replace("public", "")).body();
        });

        /* [api] admin boards dump */
        post("/admin/boards/dump", ctx -> {
            JsonResponse rsp = new JsonResponse(0);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT * FROM boards ORDER BY id ASC";
                return h.createQuery(sql).mapToMap().list();
            });
            List<String> columnList = Arrays.asList("id", "name", "ts_create");
            String filename = String.format("public/files/boards-%s.csv", System.currentTimeMillis());
            outputCsvFile(records, columnList, filename);
            return rsp.set("url", filename.replace("public", "")).body();
        });

        /* [api] admin posts dump */
        post("/admin/posts/dump", ctx -> {
            JsonResponse rsp = new JsonResponse(0);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT * FROM posts ORDER BY id ASC";
                return h.createQuery(sql).mapToMap().list();
            });
            List<String> columnList = Arrays.asList("id", "id_user", "id_board", "title", "content", "url_avatar", "ts_create");
            String filename = String.format("public/files/posts-%s.csv", System.currentTimeMillis());
            outputCsvFile(records, columnList, filename);
            return rsp.set("url", filename.replace("public", "")).body();
        });

        /* [api] admin comments dump */
        post("/admin/comments/dump", ctx -> {
            JsonResponse rsp = new JsonResponse(0);
            List<Map<String, Object>> records = jdbi.withHandle(h -> {
                String sql = "SELECT * FROM comments ORDER BY id ASC";
                return h.createQuery(sql).mapToMap().list();
            });
            List<String> columnList = Arrays.asList("id", "id_user", "id_post", "content", "ts_create");
            String filename = String.format("public/files/comments-%s.csv", System.currentTimeMillis());
            outputCsvFile(records, columnList, filename);
            return rsp.set("url", filename.replace("public", "")).body();
        });
    }

    /* response adapter */
    private static class JsonResponse {
        Map<String, Object> response;

        JsonResponse(Integer code) {
            this.response = new HashMap<>();
            this.response.put("code", code);
        }

        JsonResponse set(String key, Object value) {
            this.response.put(key, value);
            return this;
        }

        JsonResponse set(Map<String, Object> map) {
            this.response.putAll(map);
            return this;
        }

        Map<String, Object> body() {
            return this.response;
        }
    }

    /* sha 256 */
    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(text.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            return text;
        }
    }

    /* finalize datetime string */
    private static void finalizeDatetime(Pattern pattern, Map<String, Object> map, String columnName) {
        Matcher matcher = pattern.matcher((String) map.get(columnName));
        if (matcher.find()) {
            map.put(columnName, matcher.group());
        }
    }

    /* output csv with big5 encoding */
    private static void outputCsvFile(List<Map<String, Object>> records, List<String> columnList, String filename) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), "BIG5")) {
            for (String columnName : columnList) {
                if (columnList.indexOf(columnName) == columnList.size() - 1) {
                    writer.write(String.format("\"%s\"\n", columnName));
                } else {
                    writer.write(String.format("\"%s\",", columnName));
                }
            }
            for (Map<String, Object> record : records) {
                for (String columnName : columnList) {
                    if (columnList.indexOf(columnName) == columnList.size() - 1) {
                        writer.write("\"" + record.get(columnName) + "\"\n");
                    } else {
                        writer.write("\"" + record.get(columnName) + "\",");
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
