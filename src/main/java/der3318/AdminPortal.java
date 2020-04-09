package der3318;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.jooby.Jooby;
import io.jooby.ModelAndView;

public class AdminPortal extends Jooby {
    {
        /* admin page */
        get("/", ctx -> {
            Map<String, List<String>> queryMap = ctx.queryMultimap();
            List<String> tokenList = queryMap.getOrDefault("token", Arrays.asList());
            if (tokenList.contains(getEnvironment().getConfig().getString("admin.token"))) {
                return new ModelAndView("admin.html");
            }
            return new ModelAndView("base.html");
        });
    }
}
