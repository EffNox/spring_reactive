package nr.client.services;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;

import static org.springframework.http.MediaType.*;
import org.springframework.stereotype.Service;
// import static org.springframework.web.reactive.function.BodyInserters.*;
import org.springframework.web.reactive.function.client.WebClient;
import nr.client.models.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SProductoImp implements SProducto {

    private @Autowired WebClient client;

    @Override
    public Flux<Producto> findAll() {
        return client.get().accept(APPLICATION_JSON).retrieve().bodyToFlux(Producto.class);
        // .exchange().flatMapMany(rs->rs.bodyToFlux(Producto.class));
    }

    @Override
    public Mono<Producto> findById(String id) {
        return client.get().uri("/{id}", Collections.singletonMap("id", id)).accept(APPLICATION_JSON).retrieve()
                .bodyToMono(Producto.class);
        // .exchange().flatMap(rs->rs.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> save(Producto e) {
        return client.post().accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                // .body(fromValue(e))
                .bodyValue(e).retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> update(Producto e, String id) {
        return client.put().uri("/{id}", Collections.singletonMap("id", id)).accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON).bodyValue(e).retrieve().bodyToMono(Producto.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return client.delete().uri("/{id}", Collections.singletonMap("id", id)).retrieve().bodyToMono(Void.class);
    }

    @Override
    public Mono<Producto> upload(FilePart fp, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts.asyncPart("fp", fp.content(), DataBuffer.class).headers(h-> {
            h.setContentDispositionFormData("fp", fp.filename());
        });

        return client.post().uri("/upload/{id}",Collections.singletonMap("id", id)).contentType(MULTIPART_FORM_DATA)
        .bodyValue(parts.build())
        .retrieve().bodyToMono(Producto.class);
    }
    
}
