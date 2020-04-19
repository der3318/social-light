package der3318;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.jooby.MockContext;
import io.jooby.MockRouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class UnitTest {

    private static MockRouter router;

    @BeforeAll
    public static void testSetUp() {
        router = new MockRouter(new ApplicationProgrammingInterfaceVersion1());
    }

    @BeforeEach
    public void methodSetUp() {
        HashMap response = router.post("/admin/database/reset", new MockContext()).value(HashMap.class);
        assumeTrue(0 == ((Integer) response.get("code")));
    }

    @AfterEach
    public void methodCleanUp() {
        HashMap response = router.post("/admin/database/reset", new MockContext()).value(HashMap.class);
        assumeTrue(0 == ((Integer) response.get("code")));
    }

    @Test
    public void info() {
        MockContext context = new MockContext().setBody("");
        HashMap response = router.post("/info", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertEquals("der3318", response.get("developer"));
        assertEquals("2020 March 8th", response.get("date"));
        assertEquals(7, ((List<Map<String, Object>>) response.get("tables")).size());
    }

    @Test
    public void loginSucceeded() {
        MockContext context = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap response = router.post("/login", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertEquals(1, response.get("id"));
        assertNotNull(response.get("token"));
    }

    @Test
    public void loginFailed() {
        MockContext context1 = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"\"}");
        HashMap response1 = router.post("/login", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody("{\"account\":\"\", \"password\":\"password1\"}");
        HashMap response2 = router.post("/login", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertEquals(1, response1.size());
    }

    @Test
    public void getUserInfoSucceeded() {
        MockContext context = new MockContext().setBody("{\"id\":1}");
        HashMap response = router.post("/user", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertEquals("account1", response.get("account"));
        assertEquals("測試使用者一號", response.get("name"));
        assertEquals("癌症病人", response.get("type"));
        assertEquals("台北", response.get("location"));
        assertEquals("座右銘呢", response.get("motto"));
        assertEquals("首先自我介紹", response.get("intro"));
        assertEquals("user1.png", response.get("url_avatar"));
    }

    @Test
    public void getUserInfoFailed() {
        MockContext context = new MockContext().setBody("{\"id\":0}");
        HashMap response = router.post("/user", context).value(HashMap.class);
        assertEquals(-1, response.get("code"));
        assertEquals(1, response.size());
    }

    @Test
    public void updateUserInfoSucceeded() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* info cache */
        MockContext infoContext = new MockContext().setBody("{\"id\":1}");
        HashMap cachedInfo = router.post("/user", infoContext).value(HashMap.class);
        assumeTrue(8 == cachedInfo.size());
        /* try different permutation */
        List<String> targetColumnList = Arrays.asList("password", "name", "type", "location", "motto", "intro", "url_avatar");
        for (int round = 0; round < (1 << targetColumnList.size()); round++) {
            String bodyFormat = "{\"id\":1, \"token\":\"%s\"%s}";
            String paramFormat = ",\"%s\":\"%s\"";
            StringBuilder params = new StringBuilder();
            for (int targetColumnIdx = 0; targetColumnIdx < targetColumnList.size(); targetColumnIdx++) {
                String targetColumn = targetColumnList.get(targetColumnIdx);
                if (((1 << targetColumnIdx) & round) != 0) {
                    String newValue = "中文 English Emoji \uD83C\uDD97 " + new Random().nextInt();
                    cachedInfo.put(targetColumn, newValue);
                    params.append(String.format(paramFormat, targetColumn, newValue));
                }
            }
            /* verify update status */
            MockContext context = new MockContext().setBody(String.format(bodyFormat, token, params));
            HashMap response = router.post("/user/update", context).value(HashMap.class);
            assertEquals(0, response.get("code"));
            /* verify new password */
            MockContext checkLoginContext = new MockContext().setBody(String.format("{\"account\":\"account1\",\"password\":\"%s\"}", cachedInfo.getOrDefault("password", "password1")));
            HashMap checkLoginResponse = router.post("/login", checkLoginContext).value(HashMap.class);
            assertEquals(0, checkLoginResponse.get("code"));
            token = (String) checkLoginResponse.get("token");
            /* verify new info */
            MockContext checkInfoContext = new MockContext().setBody("{\"id\":1}");
            HashMap checkInfoResponse = router.post("/user", checkInfoContext).value(HashMap.class);
            for (Object key : checkInfoResponse.keySet()) {
                assertEquals(cachedInfo.getOrDefault(key, 0), checkInfoResponse.get(key));
            }
        }
    }

    @Test
    public void updateUserInfoFailed() {
        /* info cache */
        MockContext infoContext = new MockContext().setBody("{\"id\":1}");
        HashMap cachedInfo = router.post("/user", infoContext).value(HashMap.class);
        assumeTrue(8 == cachedInfo.size());
        /* try different permutation */
        List<String> targetColumnList = Arrays.asList("password", "name", "type", "location", "motto", "intro", "url_avatar");
        for (int round = 0; round < (1 << targetColumnList.size()); round++) {
            String bodyFormat1 = "{\"id\":0, \"token\":\"\"%s}";
            String bodyFormat2 = "{\"id\":1, \"token\":\"\"%s}";
            String paramFormat = ",\"%s\":\"%s\"";
            StringBuilder params = new StringBuilder();
            for (int targetColumnIdx = 0; targetColumnIdx < targetColumnList.size(); targetColumnIdx++) {
                String targetColumn = targetColumnList.get(targetColumnIdx);
                if (((1 << targetColumnIdx) & round) != 0) {
                    String newValue = "中文 English Emoji \uD83C\uDD97 " + new Random().nextInt();
                    params.append(String.format(paramFormat, targetColumn, newValue));
                }
            }
            /* verify update status */
            MockContext context1 = new MockContext().setBody(String.format(bodyFormat1, params));
            HashMap response1 = router.post("/user/update", context1).value(HashMap.class);
            assertEquals(-1, response1.get("code"));
            MockContext context2 = new MockContext().setBody(String.format(bodyFormat2, params));
            HashMap response2 = router.post("/user/update", context2).value(HashMap.class);
            assertEquals(-2, response2.get("code"));
            /* verify old password */
            MockContext checkLoginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
            HashMap checkLoginResponse = router.post("/login", checkLoginContext).value(HashMap.class);
            assertEquals(0, checkLoginResponse.get("code"));
            /* verify old info */
            MockContext checkInfoContext = new MockContext().setBody("{\"id\":1}");
            HashMap checkInfoResponse = router.post("/user", checkInfoContext).value(HashMap.class);
            assertEquals(cachedInfo, checkInfoResponse);
        }
    }

    @Test
    public void getListOfBoardsSucceeded() {
        MockContext context = new MockContext().setBody("");
        HashMap response = router.post("/boards", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        List<Map<String, Object>> boardList = (List<Map<String, Object>>) response.get("boards");
        assertEquals(2, boardList.size());
        assertEquals(1, boardList.get(0).get("id"));
        assertEquals("分類看板一", boardList.get(0).get("name"));
    }

    @Test
    public void getPostsWithFiltersSucceeded() {
        /* verify user filter and board filter */
        MockContext context1 = new MockContext().setBody("{\"policy\":0,\"id_user\":1,\"id_board\":-1}");
        HashMap response1 = router.post("/posts", context1).value(HashMap.class);
        assertEquals(0, response1.get("code"));
        List<Map<String, Object>> postList1 = (List<Map<String, Object>>) response1.get("posts");
        assertEquals(1, postList1.get(0).get("id"));
        assertEquals(1, postList1.get(0).get("id_user"));
        assertEquals(-1, postList1.get(0).get("id_board"));
        assertEquals("文章標題一", postList1.get(0).get("title"));
        assertEquals("內文，標點，第一篇", postList1.get(0).get("content"));
        assertEquals("post1.jpg", postList1.get(0).get("url_avatar"));
        String timestamp = (String) postList1.get(0).get("ts_create");
        assertEquals(0, timestamp.replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
        /* verify keyword filter and ts filter */
        MockContext context2 = new MockContext().setBody(String.format("{\"policy\":2,\"keyword\":\"文章標題\",\"ts\":\"%s\"}", timestamp));
        HashMap response2 = router.post("/posts", context2).value(HashMap.class);
        assertEquals(0, response2.get("code"));
        List<Map<String, Object>> postList2 = (List<Map<String, Object>>) response2.get("posts");
        assertEquals(4, postList2.size());
        /* verify sorting policy */
        MockContext context3 = new MockContext().setBody("{\"policy\":1}");
        HashMap response3 = router.post("/posts", context3).value(HashMap.class);
        assertEquals(0, response3.get("code"));
        List<Map<String, Object>> postList3 = (List<Map<String, Object>>) response3.get("posts");
        String latestTimestamp = (String) postList3.get(0).get("ts_create");
        for (Map<String, Object> post : postList3) {
            assertTrue(latestTimestamp.compareTo((String) post.get("ts_create")) >= 0);
        }
    }

    @Test
    public void getPostsWithFiltersFailed() {
        MockContext context = new MockContext().setBody("{\"policy\":-1}");
        HashMap response = router.post("/posts", context).value(HashMap.class);
        assertEquals(-1, response.get("code"));
        assertEquals(1, response.size());
    }

    @Test
    public void publishOrUpdatePostSucceeded() throws InterruptedException {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* publish */
        Thread.sleep(1000);
        MockContext context1 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":1,\"id_board\":1,\"title\":\"單元測試\",\"content\":\"☑\",\"url_avatar\":\"post7.jpg\"}", token));
        HashMap response1 = router.post("/post/update", context1).value(HashMap.class);
        assertEquals(0, response1.get("code"));
        assertEquals(7, response1.get("id"));
        /* verify info */
        MockContext infoContext = new MockContext().setBody("{\"policy\":1}");
        HashMap info = router.post("/posts", infoContext).value(HashMap.class);
        assumeTrue(2 == info.size());
        List<Map<String, Object>> postList = (List<Map<String, Object>>) info.get("posts");
        assertEquals(7, postList.get(0).get("id"));
        assertEquals(1, postList.get(0).get("id_user"));
        assertEquals(1, postList.get(0).get("id_board"));
        assertEquals("單元測試", postList.get(0).get("title"));
        assertEquals("☑", postList.get(0).get("content"));
        assertEquals("post7.jpg", postList.get(0).get("url_avatar"));
        /* update */
        MockContext context2 = new MockContext().setBody(String.format("{\"id\":7,\"token\":\"%s\",\"id_user\":1,\"id_board\":2,\"title\":\"改\",\"content\":\"☑☑\",\"url_avatar\":\"post7.png\"}", token));
        HashMap response2 = router.post("/post/update", context2).value(HashMap.class);
        assertEquals(0, response2.get("code"));
        assertEquals(7, response2.get("id"));
        /* verify new info */
        MockContext newInfoContext = new MockContext().setBody("{\"policy\":1}");
        HashMap newInfo = router.post("/posts", newInfoContext).value(HashMap.class);
        assumeTrue(2 == newInfo.size());
        List<Map<String, Object>> newPostList = (List<Map<String, Object>>) newInfo.get("posts");
        assertEquals(7, newPostList.get(0).get("id"));
        assertEquals(1, newPostList.get(0).get("id_user"));
        assertEquals(2, newPostList.get(0).get("id_board"));
        assertEquals("改", newPostList.get(0).get("title"));
        assertEquals("☑☑", newPostList.get(0).get("content"));
        assertEquals("post7.png", newPostList.get(0).get("url_avatar"));
    }

    @Test
    public void publishOrUpdatePostFailed() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* different status code */
        MockContext context1 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":0,\"id_board\":1,\"title\":\"標\",\"content\":\"文\",\"url_avatar\":\"jpg\"}", token));
        HashMap response1 = router.post("/post/update", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":1,\"id_board\":0,\"title\":\"標\",\"content\":\"文\",\"url_avatar\":\"jpg\"}", token));
        HashMap response2 = router.post("/post/update", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertEquals(1, response2.size());
        MockContext context3 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":1,\"id_board\":1,\"title\":\"標\",\"content\":\"文\",\"url_avatar\":\" \"}", token));
        HashMap response3 = router.post("/post/update", context3).value(HashMap.class);
        assertEquals(-3, response3.get("code"));
        assertEquals(1, response3.size());
        MockContext context4 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":2,\"id_board\":1,\"title\":\"標\",\"content\":\"文\",\"url_avatar\":\"jpg\"}", token));
        HashMap response4 = router.post("/post/update", context4).value(HashMap.class);
        assertEquals(-4, response4.get("code"));
        assertEquals(1, response4.size());
    }

    @Test
    public void getPostInfoAndRelatedCommentsSucceeded() {
        MockContext context = new MockContext().setBody("{\"id\":1}");
        HashMap response = router.post("/post", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertEquals(1, response.get("id_user"));
        assertEquals(-1, response.get("id_board"));
        assertEquals("文章標題一", response.get("title"));
        assertEquals("內文，標點，第一篇", response.get("content"));
        assertEquals("post1.jpg", response.get("url_avatar"));
        assertEquals(0, ((String) response.get("ts_create")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
        List<Map<String, Object>> commentList = (List<Map<String, Object>>) response.get("comments");
        assertEquals(1, commentList.size());
        assertEquals(1, commentList.get(0).get("id"));
        assertEquals(1, commentList.get(0).get("id_user"));
        assertEquals("文章留言一", commentList.get(0).get("content"));
        assertEquals(0, ((String) commentList.get(0).get("ts_create")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
    }

    @Test
    public void getPostInfoAndRelatedCommentsFailed() {
        MockContext context = new MockContext().setBody("{\"id\":0}");
        HashMap response = router.post("/post", context).value(HashMap.class);
        assertEquals(-1, response.get("code"));
        assertEquals(1, response.size());
    }

    @Test
    public void publishOrUpdateCommentSucceeded() throws InterruptedException {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* publish */
        Thread.sleep(1000);
        MockContext context1 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":1,\"id_post\":1,\"content\":\"☑\"}", token));
        HashMap response1 = router.post("/comment/update", context1).value(HashMap.class);
        assertEquals(0, response1.get("code"));
        assertEquals(10, response1.get("id"));
        /* verify info */
        MockContext infoContext = new MockContext().setBody("{\"id\":1}");
        HashMap info = router.post("/post", infoContext).value(HashMap.class);
        assumeTrue(8 == info.size());
        List<Map<String, Object>> commentList = (List<Map<String, Object>>) info.get("comments");
        assertEquals(10, commentList.get(1).get("id"));
        assertEquals(1, commentList.get(1).get("id_user"));
        assertEquals("☑", commentList.get(1).get("content"));
        /* update */
        MockContext context2 = new MockContext().setBody(String.format("{\"id\":10,\"token\":\"%s\",\"id_user\":1,\"id_post\":2,\"content\":\"☑☑\"}", token));
        HashMap response2 = router.post("/comment/update", context2).value(HashMap.class);
        assertEquals(0, response2.get("code"));
        assertEquals(10, response2.get("id"));
        /* verify new info */
        MockContext newInfoContext = new MockContext().setBody("{\"id\":2}");
        HashMap newInfo = router.post("/post", newInfoContext).value(HashMap.class);
        assumeTrue(8 == newInfo.size());
        List<Map<String, Object>> newCommentList = (List<Map<String, Object>>) newInfo.get("comments");
        assertEquals(10, newCommentList.get(2).get("id"));
        assertEquals(1, newCommentList.get(2).get("id_user"));
        assertEquals("☑☑", newCommentList.get(2).get("content"));
    }

    @Test
    public void publishOrUpdateCommentFailed() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* different status code */
        MockContext context1 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":0,\"id_post\":1,\"content\":\"☑\"}", token));
        HashMap response1 = router.post("/comment/update", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":1,\"id_post\":0,\"content\":\"☑\"}", token));
        HashMap response2 = router.post("/comment/update", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertEquals(1, response2.size());
        MockContext context3 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":1,\"id_post\":1,\"content\":\" \"}", token));
        HashMap response3 = router.post("/comment/update", context3).value(HashMap.class);
        assertEquals(-3, response3.get("code"));
        assertEquals(1, response3.size());
        MockContext context4 = new MockContext().setBody(String.format("{\"token\":\"%s\",\"id_user\":2,\"id_post\":1,\"content\":\"☑\"}", token));
        HashMap response4 = router.post("/comment/update", context4).value(HashMap.class);
        assertEquals(-4, response4.get("code"));
        assertEquals(1, response4.size());
    }

    @Test
    public void getChatroomsOfUserSucceeded() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* verify info */
        MockContext context = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\"}", token));
        HashMap response = router.post("/chatrooms", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        List<Map<String, Object>> chatroomList = (List<Map<String, Object>>) response.get("chatrooms");
        assertEquals(1, chatroomList.size());
        assertEquals(1, chatroomList.get(0).get("id"));
        assertEquals(2, chatroomList.get(0).get("id_user_target"));
        assertEquals("測試使用者二號", chatroomList.get(0).get("name"));
        assertEquals("user2.png", chatroomList.get(0).get("url_avatar"));
        assertEquals(2, chatroomList.get(0).get("lastmsg_status"));
        assertEquals("訊息測試", chatroomList.get(0).get("lastmsg_content"));
        assertEquals(0, ((String) chatroomList.get(0).get("lastmsg_ts")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
    }

    @Test
    public void getChatroomsOfUserFailed() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* different status code */
        MockContext context1 = new MockContext().setBody(String.format("{\"id\":0,\"token\":\"%s\"}", token));
        HashMap response1 = router.post("/chatrooms", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody(String.format("{\"id\":2,\"token\":\"%s\"}", token));
        HashMap response2 = router.post("/chatrooms", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertEquals(1, response2.size());
    }

    @Test
    public void updateChatroomSucceeded() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* update */
        MockContext context = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user\":1,\"name\":\"☑\",\"url_avatar\":\"chatroom.jpg\"}", token));
        HashMap response = router.post("/chatroom/update", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        /* verify new info */
        MockContext newInfoContext = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\"}", token));
        HashMap newInfo = router.post("/chatrooms", newInfoContext).value(HashMap.class);
        assumeTrue(2 == newInfo.size());
        List<Map<String, Object>> newChatroomList = (List<Map<String, Object>>) newInfo.get("chatrooms");
        assertEquals(1, newChatroomList.get(0).get("id"));
        assertEquals(2, newChatroomList.get(0).get("id_user_target"));
        assertEquals("☑", newChatroomList.get(0).get("name"));
        assertEquals("chatroom.jpg", newChatroomList.get(0).get("url_avatar"));
        assertEquals(2, newChatroomList.get(0).get("lastmsg_status"));
        assertEquals("訊息測試", newChatroomList.get(0).get("lastmsg_content"));
        assertEquals(0, ((String) newChatroomList.get(0).get("lastmsg_ts")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
    }

    @Test
    public void updateChatroomFailed() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* different status code */
        MockContext context1 = new MockContext().setBody(String.format("{\"id\":0,\"token\":\"%s\",\"id_user\":1,\"name\":\"☑\",\"url_avatar\":\"chatroom.jpg\"}", token));
        HashMap response1 = router.post("/chatroom/update", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user\":0,\"name\":\"☑\",\"url_avatar\":\"chatroom.jpg\"}", token));
        HashMap response2 = router.post("/chatroom/update", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertEquals(1, response2.size());
        MockContext context3 = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user\":2,\"name\":\"☑\",\"url_avatar\":\"chatroom.jpg\"}", token));
        HashMap response3 = router.post("/chatroom/update", context3).value(HashMap.class);
        assertEquals(-3, response3.get("code"));
        assertEquals(1, response3.size());
    }

    @Test
    public void getMessagesBetweenUsersSucceeded() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account2\",\"password\":\"password2\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* verify info */
        MockContext context = new MockContext().setBody(String.format("{\"id\":2,\"token\":\"%s\",\"id_user_target\":1}", token));
        HashMap response = router.post("/messages", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertEquals(2, response.get("id_chatroom"));
        assertEquals("測試使用者一號", response.get("name"));
        assertEquals("user1.png", response.get("url_avatar"));
        List<Map<String, Object>> messageList = (List<Map<String, Object>>) response.get("messages");
        assertEquals(1, messageList.size());
        assertEquals(1, messageList.get(0).get("status"));
        assertEquals("訊息測試", messageList.get(0).get("content"));
        assertEquals(0, ((String) messageList.get(0).get("ts_create")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
    }

    @Test
    public void getMessagesBetweenUsersFailed() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* different status code */
        MockContext context1 = new MockContext().setBody(String.format("{\"id\":0,\"token\":\"%s\",\"id_user_target\":2}", token));
        HashMap response1 = router.post("/messages", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user_target\":3}", token));
        HashMap response2 = router.post("/messages", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertEquals(1, response2.size());
        MockContext context3 = new MockContext().setBody(String.format("{\"id\":2,\"token\":\"%s\",\"id_user_target\":1}", token));
        HashMap response3 = router.post("/messages", context3).value(HashMap.class);
        assertEquals(-3, response3.get("code"));
        assertEquals(1, response3.size());
    }

    @Test
    public void sendMessageSucceeded() throws InterruptedException {
        /* login */
        MockContext loginContext1 = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse1 = router.post("/login", loginContext1).value(HashMap.class);
        String token1 = (String) loginResponse1.get("token");
        assumeTrue(null != token1);
        MockContext loginContext2 = new MockContext().setBody("{\"account\":\"account2\",\"password\":\"password2\"}");
        HashMap loginResponse2 = router.post("/login", loginContext2).value(HashMap.class);
        String token2 = (String) loginResponse2.get("token");
        assumeTrue(null != token2);
        MockContext loginContext3 = new MockContext().setBody("{\"account\":\"account3\",\"password\":\"password3\"}");
        HashMap loginResponse3 = router.post("/login", loginContext3).value(HashMap.class);
        String token3 = (String) loginResponse3.get("token");
        assumeTrue(null != token2);
        /* send message to existed room */
        Thread.sleep(1000);
        MockContext context = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user_target\":2,\"content\":\"繼續對話\"}", token1));
        HashMap response = router.post("/message/update", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        /* verify info from sender */
        MockContext infoContext1 = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\"}", token1));
        HashMap info1 = router.post("/chatrooms", infoContext1).value(HashMap.class);
        assumeTrue(2 == info1.size());
        List<Map<String, Object>> chatroomList1 = (List<Map<String, Object>>) info1.get("chatrooms");
        assertEquals(1, chatroomList1.size());
        assertEquals(1, chatroomList1.get(0).get("id"));
        assertEquals(2, chatroomList1.get(0).get("id_user_target"));
        assertEquals("測試使用者二號", chatroomList1.get(0).get("name"));
        assertEquals("user2.png", chatroomList1.get(0).get("url_avatar"));
        assertEquals(2, chatroomList1.get(0).get("lastmsg_status"));
        assertEquals("繼續對話", chatroomList1.get(0).get("lastmsg_content"));
        assertEquals(0, ((String) chatroomList1.get(0).get("lastmsg_ts")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
        /* verify info from receiver */
        MockContext infoContext2 = new MockContext().setBody(String.format("{\"id\":2,\"token\":\"%s\"}", token2));
        HashMap info2 = router.post("/chatrooms", infoContext2).value(HashMap.class);
        assumeTrue(2 == info2.size());
        List<Map<String, Object>> chatroomList2 = (List<Map<String, Object>>) info2.get("chatrooms");
        assertEquals(1, chatroomList2.size());
        assertEquals(2, chatroomList2.get(0).get("id"));
        assertEquals(1, chatroomList2.get(0).get("id_user_target"));
        assertEquals("測試使用者一號", chatroomList2.get(0).get("name"));
        assertEquals("user1.png", chatroomList2.get(0).get("url_avatar"));
        assertEquals(0, chatroomList2.get(0).get("lastmsg_status"));
        assertEquals("繼續對話", chatroomList2.get(0).get("lastmsg_content"));
        assertEquals(0, ((String) chatroomList2.get(0).get("lastmsg_ts")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
        /* send message to new room */
        Thread.sleep(1000);
        MockContext newContext = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user_target\":3,\"content\":\"開始聊天\"}", token1));
        HashMap newResponse = router.post("/message/update", newContext).value(HashMap.class);
        assertEquals(0, newResponse.get("code"));
        /* verify new info from sender */
        MockContext newInfoContext1 = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\"}", token1));
        HashMap newInfo1 = router.post("/chatrooms", newInfoContext1).value(HashMap.class);
        assumeTrue(2 == newInfo1.size());
        List<Map<String, Object>> newChatroomList1 = (List<Map<String, Object>>) newInfo1.get("chatrooms");
        assertEquals(2, newChatroomList1.size());
        assertEquals(3, newChatroomList1.get(0).get("id"));
        assertEquals(3, newChatroomList1.get(0).get("id_user_target"));
        assertEquals("測試使用者三號", newChatroomList1.get(0).get("name"));
        assertEquals("user3.png", newChatroomList1.get(0).get("url_avatar"));
        assertEquals(2, newChatroomList1.get(0).get("lastmsg_status"));
        assertEquals("開始聊天", newChatroomList1.get(0).get("lastmsg_content"));
        assertEquals(0, ((String) newChatroomList1.get(0).get("lastmsg_ts")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
        /* verify new info from receiver */
        MockContext newInfoContext2 = new MockContext().setBody(String.format("{\"id\":3,\"token\":\"%s\"}", token3));
        HashMap newInfo2 = router.post("/chatrooms", newInfoContext2).value(HashMap.class);
        assumeTrue(2 == newInfo2.size());
        List<Map<String, Object>> newChatroomList2 = (List<Map<String, Object>>) newInfo2.get("chatrooms");
        assertEquals(1, newChatroomList2.size());
        assertEquals(4, newChatroomList2.get(0).get("id"));
        assertEquals(1, newChatroomList2.get(0).get("id_user_target"));
        assertEquals("測試使用者一號", newChatroomList2.get(0).get("name"));
        assertEquals("user1.png", newChatroomList2.get(0).get("url_avatar"));
        assertEquals(0, newChatroomList2.get(0).get("lastmsg_status"));
        assertEquals("開始聊天", newChatroomList2.get(0).get("lastmsg_content"));
        assertEquals(0, ((String) newChatroomList2.get(0).get("lastmsg_ts")).replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}", "").length());
    }

    @Test
    public void sendMessageFailed() {
        /* login */
        MockContext loginContext = new MockContext().setBody("{\"account\":\"account1\",\"password\":\"password1\"}");
        HashMap loginResponse = router.post("/login", loginContext).value(HashMap.class);
        String token = (String) loginResponse.get("token");
        assumeTrue(null != token);
        /* different status code */
        MockContext context1 = new MockContext().setBody(String.format("{\"id\":1,\"token\":\"%s\",\"id_user_target\":0,\"content\":\"繼續對話\"}", token));
        HashMap response1 = router.post("/message/update", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertEquals(1, response1.size());
        MockContext context2 = new MockContext().setBody(String.format("{\"id\":0,\"token\":\"%s\",\"id_user_target\":2,\"content\":\"繼續對話\"}", token));
        HashMap response2 = router.post("/message/update", context2).value(HashMap.class);
        assertEquals(-1, response2.get("code"));
        assertEquals(1, response2.size());
        MockContext context3 = new MockContext().setBody(String.format("{\"id\":2,\"token\":\"%s\",\"id_user_target\":1,\"content\":\"繼續對話\"}", token));
        HashMap response3 = router.post("/message/update", context3).value(HashMap.class);
        assertEquals(-2, response3.get("code"));
        assertEquals(1, response3.size());
    }

}
