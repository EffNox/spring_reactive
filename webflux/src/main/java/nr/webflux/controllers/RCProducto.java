package nr.webflux.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import nr.webflux.models.documents.Producto;
import nr.webflux.models.services.SProducto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/productos")
public class RCProducto {
    private @Autowired SProducto sv;
    private Logger lg = LoggerFactory.getLogger(CProducto.class);

    @GetMapping
    public Flux<Producto> getAll() {
        Flux<Producto> p = sv.getAll().map(pr->{
            pr.setNombre(pr.getNombre().toUpperCase());
            return pr;
        }).doOnNext(pr-> lg.info(pr.toString()));
        return p;
    }
    
    @GetMapping("/{id}")
    public Mono<Producto> getAll(@PathVariable String id) {
        // Mono<Producto> p = sv.findById(id);
        Flux<Producto> prs = sv.getAll();
        Mono<Producto> p = prs.filter(pr-> pr.getId().equals(id))
        .next()
        .doOnNext(pr-> lg.info(pr.toString()));
        return p;
    }
}
