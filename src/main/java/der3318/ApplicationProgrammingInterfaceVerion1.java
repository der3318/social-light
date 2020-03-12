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

public class ApplicationProgrammingInterfaceVerion1 extends Jooby {
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
                params = req.containsKey("ts") ? params + " AND ts_create >= DATE(:ts)" : params;
                String sql = String.format("SELECT id, id_user, id_board, title, content, url_avatar, ts_create FROM posts WHERE %s", params);
                return h.createQuery(sql).bindMap(req).mapToMap().list();
            });
            for (Map<String, Object> record : records) {
                Matcher matcher = timePattern.matcher((String) record.get("ts_create"));
                if (matcher.find()) {
                    record.put("ts_create", matcher.group());
                }
            }
            return rsp.set("posts", records).body();
        });
    }

    /* response wrapper */
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
}
