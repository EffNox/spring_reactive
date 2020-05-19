package nr.rest.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import nr.rest.models.documents.Categoria;
import nr.rest.models.documents.Producto;
import nr.rest.models.services.SProducto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class HProducto {

    private @Autowired SProducto sv;
    private @Value("${config.uploads.path}") String path;
    private @Autowired Validator validator;

    public Mono<ServerResponse> getAll(ServerRequest rq) {
        return ServerResponse.ok().body(sv.getAll(), Producto.class);
    }

    public Mono<ServerResponse> get(ServerRequest rq) {
        String id = rq.pathVariable("id");
        return sv.get(id).flatMap(p -> ServerResponse.ok().body(fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest rq) {
        Mono<Producto> producto = rq.bodyToMono(Producto.class);
        return producto.flatMap(p -> {
            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors()).map(fieldError -> "El campo "+ fieldError.getField()+ " " + fieldError.getDefaultMessage())
                .collectList().flatMap(lst->ServerResponse.badRequest().body(fromValue(lst)));
            } else {
                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }
                return sv.create(p)
                .flatMap(pdb -> ServerResponse.created(URI.create("v2/productos/" + pdb.getId())).body(fromValue(pdb)));
            }
        });
    }

    public Mono<ServerResponse> update(ServerRequest rq) {
        String id = rq.pathVariable("id");
        Mono<Producto> producto = rq.bodyToMono(Producto.class);
        Mono<Producto> pDB = sv.get(id);
        return pDB.zipWith(producto, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setPrecio(req.getPrecio());
            db.setCategoria(req.getCategoria());
            return db;
        }).flatMap(
                p -> ServerResponse.created(URI.create("v2/productos/" + p.getId())).body(sv.create(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest rq) {
        String id = rq.pathVariable("id");
        Mono<Producto> pDB = sv.get(id);
        return pDB.flatMap(p -> sv.delete(p).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> upload(ServerRequest rq) {
        String id = rq.pathVariable("id");
        return rq.multipartData().map(multipart -> multipart.toSingleValueMap().get("fp")).cast(FilePart.class)
                .flatMap(fl -> sv.get(id).flatMap(p -> {
                    p.setFile(UUID.randomUUID().toString() + "_"
                            + fl.filename().replace(" ", "").replace(":", "").replace("\\", ""));
                    return fl.transferTo(new File(path + p.getFile())).then(sv.create(p));
                })).flatMap(p -> ServerResponse.created((URI.create("v2/productos/" + p.getId()))).body(fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> uploadv2(ServerRequest rq) {
        Mono<Producto> producto = rq.multipartData().map(mt -> {
            FormFieldPart nombre = (FormFieldPart) mt.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) mt.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) mt.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) mt.toSingleValueMap().get("categoria.nombre");
            Categoria categoria = new Categoria(null, categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });

        return rq.multipartData().map(multipart -> multipart.toSingleValueMap().get("fp")).cast(FilePart.class)
                .flatMap(fl -> producto.flatMap(p -> {
                    p.setFile(UUID.randomUUID().toString() + "_"
                            + fl.filename().replace(" ", "").replace(":", "").replace("\\", ""));
                    p.setCreateAt(new Date());
                    return fl.transferTo(new File(path + p.getFile())).then(sv.create(p));
                })).flatMap(p -> ServerResponse.created((URI.create("v2/productos/" + p.getId()))).body(fromValue(p)));
    }

}
