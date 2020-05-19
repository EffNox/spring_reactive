package nr.webflux.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import nr.webflux.models.documents.Producto;

public interface RProducto extends ReactiveMongoRepository<Producto, String> {

}
