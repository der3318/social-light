package der3318;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jooby.MockContext;
import io.jooby.MockRouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UnitTest {

    @Test
    public void info() {
        MockRouter router = new MockRouter(new ApplicationProgrammingInterfaceVersion1());
        MockContext context = new MockContext().setBody("");
        HashMap response = router.post("/info", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertEquals("der3318", response.get("developer"));
        assertEquals("2020 March 8th", response.get("date"));
        assertEquals(7, ((List<Map<String, Object>>) response.get("tables")).size());
    }

    @Test
    public void loginSucceeded() {
        MockRouter router = new MockRouter(new ApplicationProgrammingInterfaceVersion1());
        MockContext context = new MockContext().setBody("{\"account\":\"account1\", \"password\":\"password1\"}");
        HashMap response = router.post("/login", context).value(HashMap.class);
        assertEquals(0, response.get("code"));
        assertNotNull(response.get("token"));
        assertEquals(1, response.get("id"));
    }

    @Test
    public void loginFailed() {
        MockRouter router = new MockRouter(new ApplicationProgrammingInterfaceVersion1());
        MockContext context1 = new MockContext().setBody("{\"account\":\"account1\", \"password\":\"\"}");
        HashMap response1 = router.post("/login", context1).value(HashMap.class);
        assertEquals(-1, response1.get("code"));
        assertNull(response1.get("token"));
        assertNull(response1.get("id"));
        MockContext context2 = new MockContext().setBody("{\"account\":\"\", \"password\":\"password1\"}");
        HashMap response2 = router.post("/login", context2).value(HashMap.class);
        assertEquals(-2, response2.get("code"));
        assertNull(response2.get("token"));
        assertNull(response2.get("id"));
    }

}
