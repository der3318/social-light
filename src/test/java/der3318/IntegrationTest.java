package der3318;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.jooby.Jooby;
import io.jooby.JoobyTest;
import io.jooby.StatusCode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JoobyTest(ApplicationProgrammingInterfaceVersion1.class)
public class IntegrationTest {

    private static OkHttpClient client = new OkHttpClient();
    private static MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static ObjectMapper mapper = new ObjectMapper();
    private static TypeReference<HashMap<String, Object>> mapRef = new TypeReference<HashMap<String, Object>>() {
    };

    @Test
    public void shouldShowInfo(int serverPort, Jooby jooby) throws IOException {
        RequestBody requestBody = RequestBody.create(mediaType, "");
        Request req = new Request.Builder().post(requestBody).url("http://localhost:" + serverPort + "/info").build();
        try (Response rsp = client.newCall(req).execute()) {
            String rspString = new String(Objects.requireNonNull(rsp.body()).bytes(), StandardCharsets.UTF_8);
            jooby.getLog().info(rspString);
            Map<String, Object> rspJson = mapper.readValue(rspString, mapRef);
            assertEquals(StatusCode.OK.value(), rsp.code());
            assertEquals(0, rspJson.get("code"));
            assertEquals("der3318", rspJson.get("developer"));
            assertEquals("2020 March 8th", rspJson.get("date"));
            assertEquals(7, ((List<Map<String, Object>>) rspJson.get("tables")).size());
        }
    }

}
