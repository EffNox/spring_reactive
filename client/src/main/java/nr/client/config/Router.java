package nr.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;
import nr.client.handler.HProducto;

@Configuration
public class Router {
    
    @Bean
    public RouterFunction<ServerResponse> routes(HProducto hd){
        return route(GET("/client"), hd::getAll)
        .andRoute(GET("/client/{id}"), hd::get)
        .andRoute(POST("/client"), hd::create)
        .andRoute(PUT("/client/{id}"), hd::update)
        .andRoute(DELETE("/client/{id}"), hd::delete)
        .andRoute(POST("/client/upload/{id}"), hd::upload)
        ;
    }
    
}
