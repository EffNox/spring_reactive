package nr.rest.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import nr.rest.models.documents.Producto;
import reactor.core.publisher.Mono;

public interface RProducto extends ReactiveMongoRepository<Producto, String> {
    public Mono<Producto> findByNombre(String nombre);
}
