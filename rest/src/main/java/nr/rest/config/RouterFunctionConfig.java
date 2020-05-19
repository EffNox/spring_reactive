package nr.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import nr.rest.handler.HProducto;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(HProducto hd) {
        return route(GET("/v2/productos").or(GET("v3/productos")), hd::getAll)
        .andRoute(GET("/v2/productos/{id}"), hd::get)
        .andRoute(POST("/v2/productos"), hd::create)
        .andRoute(POST("/v2/productos/crear"), hd::uploadv2)
        .andRoute(PUT("/v2/productos/{id}"), hd::update)
        .andRoute(DELETE("/v2/productos/{id}"), hd::delete)
        .andRoute(POST("/v2/productos/upload/{id}"), hd::upload)
        ;
    }



    // private @Autowired SProducto sv;
    // @Bean
    // public RouterFunction<ServerResponse> routes() {
    // return route(GET("/v2/productos").or(GET("v3/productos")), rq -> {
    // return ServerResponse.ok().body(sv.getAll(), Producto.class);
    // });
    // }

}
