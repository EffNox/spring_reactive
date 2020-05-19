package nr.client.services;

import org.springframework.http.codec.multipart.FilePart;

import nr.client.models.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SProducto {
    public Flux<Producto> findAll();

    public Mono<Producto> findById(String id);

    public Mono<Producto> save(Producto e);

    public Mono<Producto> update(Producto e, String id);

    public Mono<Void> delete(String id);

    public Mono<Producto> upload(FilePart fp, String id);
}
