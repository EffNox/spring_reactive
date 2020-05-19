package nr.rest.models.services;

import nr.rest.models.documents.Categoria;
import nr.rest.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SProducto {
    public Flux<Producto> getAll();

    public Flux<Producto> getAllNombreUpperCase();

    public Flux<Producto> getAllNombreUpperCaseRepeat();

    public Mono<Producto> get(String id);

    public Mono<Producto> getByNombre(String nombre);

    public Mono<Categoria> findCategoriaByNombre(String nombre);

    public Mono<Producto> create(Producto e);

    public Mono<Void> delete(Producto e);

    public Flux<Categoria> getAllCategoria();

    public Mono<Categoria> getCategoria(String id);

    public Mono<Categoria> createCategoria(Categoria e);
}
