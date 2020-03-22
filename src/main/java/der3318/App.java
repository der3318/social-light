package der3318;

import io.jooby.ServerOptions;
import io.jooby.pebble.PebbleModule;

import static io.jooby.Jooby.runApp;

public class App {

    public static void main(final String[] args) {

        /**
         *
         *  Documents
         *
         *  1. https://jooby.io/#modules
         *  2. https://mkyong.com/logging/logback-xml-example/
         *  3. https://jdbi.org/
         *
         */

        runApp(args, app -> {

            /* config and ports */
            int port = 3319, securePort = 3320;
            app.setServerOptions(ServerOptions.from(app.getConfig()).get().setPort(port).setSecurePort(securePort));

            /* templates and assets */
            app.install(new PebbleModule("public/views"));
            app.assets("/images/*", "public/images");
            app.assets("/icons/*", "public/icons");
            app.assets("/javascripts/*", "public/javascripts");

            /* log info */
            app.decorator(next -> ctx -> {
                long start = System.currentTimeMillis();
                Object response = next.apply(ctx);
                long took = System.currentTimeMillis() - start;
                String format = " (%d ms)\n[IP] %-20s [METHOD] %-10s [PATH] %-20s \n[PARAMS] %s\n[BODY] %s\n";
                String ip = ctx.getRemoteAddress();
                String method = ctx.getMethod();
                String path = ctx.getRequestPath();
                String params = ctx.queryString().isEmpty() ? "NONE" : ctx.queryString();
                String body = ctx.body().bytes().length == 0 ? "NONE" : new String(ctx.body().bytes(), "UTF-8");
                synchronized (app) {
                    String msg = String.format(format, took, ip, method, path, params, body);
                    app.getLog().info(msg);
                }
                return response;
            });

            /* routes of api v1 */
            app.use("/api/v1", new ApplicationProgrammingInterfaceVersion1());

            /* routes of admin portal */
            app.use("/admin", new AdminPortal());

        });

    }

}
