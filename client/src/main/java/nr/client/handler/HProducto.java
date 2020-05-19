package nr.client.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import static org.springframework.http.MediaType.*;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import nr.client.models.Producto;
import nr.client.services.SProducto;
import reactor.core.publisher.Mono;

@Component
public class HProducto {
    private @Autowired SProducto sv;

    public Mono<ServerResponse> getAll(ServerRequest rq) {
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(sv.findAll(), Producto.class);
    };

    public Mono<ServerResponse> get(ServerRequest rq) {
        String id = rq.pathVariable("id");
        return erHandler(sv.findById(id).flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build()));
    };

    public Mono<ServerResponse> create(ServerRequest rq) {
        Mono<Producto> producto = rq.bodyToMono(Producto.class);
        return producto.flatMap(p -> sv.save(p)).flatMap(p -> ServerResponse
                .created(URI.create("/client/".concat(p.getId()))).contentType(APPLICATION_JSON)
                .bodyValue(p))
                .onErrorResume(er->{
                    WebClientResponseException erRs = (WebClientResponseException) er;
                    if (erRs.getStatusCode()==HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest().contentType(APPLICATION_JSON)
                        .bodyValue(erRs.getResponseBodyAsString());
                    }
                    return Mono.error(erRs);
                });
    }

    public Mono<ServerResponse> update(ServerRequest rq) {
        String id = rq.pathVariable("id");
        Mono<Producto> pr = rq.bodyToMono(Producto.class);
        return erHandler(pr.flatMap(p -> sv.update(p, id)).flatMap(p-> ServerResponse.created(URI.create("/client/".concat(id)))
        .contentType(APPLICATION_JSON)
        .bodyValue(p)));
    }

    public Mono<ServerResponse> delete(ServerRequest rq) {
        String id = rq.pathVariable("id");
        return erHandler(sv.delete(id).then(ServerResponse.noContent().build())
        .onErrorResume(er->{
            WebClientResponseException erRs = (WebClientResponseException) er;
            if (erRs.getStatusCode()==HttpStatus.NOT_FOUND) {
                return ServerResponse.notFound().build();
            }
            return Mono.error(erRs);
        }));
    }

    public Mono<ServerResponse> upload (ServerRequest rq){
        String id = rq.pathVariable("id");
        return erHandler(rq.multipartData().map(mpt-> mpt.toSingleValueMap().get("fp")).cast(FilePart.class)
        .flatMap(fl->sv.upload(fl, id))
        .flatMap(p->ServerResponse.created(URI.create("/client/".concat(id)))
        .contentType(APPLICATION_JSON).bodyValue(p)));
    }

    private Mono<ServerResponse> erHandler(Mono<ServerResponse> rs){
      return  rs.onErrorResume(er->{
            WebClientResponseException erRs = (WebClientResponseException) er;
            if (erRs.getStatusCode()==HttpStatus.NOT_FOUND) {
                Map<String,Object> body = new HashMap<>();
                body.put("er", "No existe el producto: ".concat(erRs.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", erRs.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
            }
            return Mono.error(erRs);
        });
    }
}
