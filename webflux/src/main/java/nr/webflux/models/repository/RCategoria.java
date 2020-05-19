package nr.webflux.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import nr.webflux.models.documents.Categoria;

public interface RCategoria extends ReactiveMongoRepository<Categoria, String> {

}
