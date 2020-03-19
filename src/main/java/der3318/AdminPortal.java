package der3318;

import org.jdbi.v3.core.Jdbi;

import io.jooby.Jooby;
import io.jooby.ModelAndView;
import io.jooby.hikari.HikariModule;
import io.jooby.jdbi.JdbiModule;

public class AdminPortal extends Jooby {
    {
        /* database interface */
        install(new HikariModule());
        install(new JdbiModule());
        Jdbi jdbi = require(Jdbi.class);

        /* admin page */
        get("/", ctx -> new ModelAndView("admin.html"));
    }
}
