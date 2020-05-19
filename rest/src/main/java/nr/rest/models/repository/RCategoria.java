package nr.rest.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import nr.rest.models.documents.Categoria;
import reactor.core.publisher.Mono;

public interface RCategoria extends ReactiveMongoRepository<Categoria, String> {
    Mono<Categoria> findByNombre(String nombre);
}
