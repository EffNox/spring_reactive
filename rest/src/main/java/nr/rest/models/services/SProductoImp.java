package nr.rest.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nr.rest.models.documents.Categoria;
import nr.rest.models.documents.Producto;
import nr.rest.models.repository.RCategoria;
import nr.rest.models.repository.RProducto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SProductoImp implements SProducto {

    private @Autowired RProducto rp;
    private @Autowired RCategoria rpCat;

    @Override
    public Flux<Producto> getAll() {
        return rp.findAll();
    }

    @Override
    public Mono<Producto> get(String id) {
        return rp.findById(id);
    }

    @Override
    public Mono<Producto> create(Producto e) {
        return rp.save(e);
    }

    @Override
    public Mono<Void> delete(Producto e) {
        return rp.delete(e);
    }

    @Override
    public Flux<Producto> getAllNombreUpperCase() {
        return rp.findAll().map(pr -> {
            pr.setNombre(pr.getNombre().toUpperCase());
            return pr;
        });
    }

    @Override
    public Flux<Producto> getAllNombreUpperCaseRepeat() {
        return getAllNombreUpperCase().repeat(5000);
    }

    @Override
    public Flux<Categoria> getAllCategoria() {
        return rpCat.findAll();
    }

    @Override
    public Mono<Categoria> getCategoria(String id) {
        return rpCat.findById(id);
    }

    @Override
    public Mono<Categoria> createCategoria(Categoria e) {
        return rpCat.save(e);
    }

    @Override
    public Mono<Producto> getByNombre(String nombre) {
        return rp.findByNombre(nombre);
    }

    @Override
    public Mono<Categoria> findCategoriaByNombre(String nombre) {
        return rpCat.findByNombre(nombre);
    }
    
}
