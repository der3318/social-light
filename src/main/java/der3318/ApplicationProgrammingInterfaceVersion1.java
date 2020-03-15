package der3318;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlStatements;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            ctx.setResponseType(MediaType.json);
            return mapper.writeValueAsString(next.apply(ctx));
        });

        /* json to hash map */
        TypeReference<HashMap<String, Object>> mapRef = new TypeReference<HashMap<String, Object>>() {
        };

        /* tokens for authentication */
        HashMap<Integer, String> tokens = new HashMap<>();

        /* time pattern */
        Pattern timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");

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
                StringBuilder params = new StringBuilder()
                        .append("password = :password, ")
                        .append("name = :name, ")
                        .append("type = :type, ")
                        .append("location = :location, ")
                        .append("motto = :motto, ")
                        .append("intro = :intro, ")
                        .append("url_avatar = :url_avatar");
                String sql = String.format("UPDATE users SET %s WHERE id = :id", params);
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
            if (title.isEmpty() || content.isEmpty() || avatar.isEmpty()) {
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
                    String params = "id_user = :id_user, id_board = :id_board, title = :title, content = :content, url_avatar = :url_avatar";
                    sql = String.format("UPDATE posts SET %s WHERE id = :id", params);
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if(newRecord.isPresent()) {
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
            finalizeDatetime(timePattern, record.get(), "ts_create");
            for (Map<String, Object> r : records) {
                finalizeDatetime(timePattern, r, "ts_create");
            }
            return rsp.set(record.get()).set("comments", records).body();
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
                    String params = "id_user = :id_user, id_post = :id_post, content = :content";
                    sql = String.format("UPDATE comments SET %s WHERE id = :id", params);
                }
                return h.createUpdate(sql).bindMap(req).executeAndReturnGeneratedKeys("id").mapTo(Integer.class).findFirst();
            });
            if(newRecord.isPresent()) {
                return rsp.set("id", newRecord.get()).body();
            }
            return rsp.set("id", id).body();
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
}
