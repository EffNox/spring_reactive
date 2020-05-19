package nr.rest.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import nr.rest.models.documents.Producto;
import nr.rest.models.services.SProducto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/productos")
public class CProducto {

    private @Autowired SProducto sv;
    private @Value("${config.uploads.path}") String path;
    Logger lg = LoggerFactory.getLogger(CProducto.class);

    // Advertisement jsonAd = new ObjectMapper().readValue(adString,
    // Advertisement.class);

    // @GetMapping(value = "/f")
    // public byte[] getImage(@RequestParam(value = "i") Long i) throws IOException
    // {
    // return Files.readAllBytes(new File(UPL_DIR +
    // svGaleria.findOne(i).getNombre()).toPath());
    // }

    @GetMapping
    public Mono<ResponseEntity<?>> getAll() {
        return Mono.just(ResponseEntity.ok(sv.getAll()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> get(@PathVariable String id) {
        return sv.get(id).map(p -> ResponseEntity.ok(p)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<Producto> mE) {
        Map<String, Object> rs = new HashMap<>();
        return mE.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return sv.create(p).map(pr -> {
                rs.put("dt", pr);
                return ResponseEntity.created(URI.create("/productos/" + pr.getId())).body(rs);
            });
        }).onErrorResume(t -> {
            return Mono.just(t).cast(WebExchangeBindException.class).flatMap(e -> Mono.just(e.getFieldErrors()))
                    .flatMapMany(Flux::fromIterable)
                    .map(fiEr -> "El campo " + fiEr.getField() + " " + fiEr.getDefaultMessage())
                    .collectList().flatMap(lst -> {
                        rs.put("er", lst);
                        return Mono.just(ResponseEntity.badRequest().body(rs));
                    });
        });
    }

    @PostMapping("/v2")
    public Mono<ResponseEntity<?>> createWithFile(Producto e, @RequestPart FilePart fp) {
        if (e.getCreateAt() == null) {
            e.setCreateAt(new Date());
        }
        e.setFile(UUID.randomUUID().toString() + fp.filename().replace(" ", "").replace(":", "").replace("\\", ""));
        return fp.transferTo(new File(path + e.getFile())).then(sv.create(e))
                .map(p -> ResponseEntity.created(URI.create("/productos/" + p.getId())).body(p));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> update(@PathVariable String id, @RequestBody Producto e) {
        return sv.get(id).flatMap(p -> {
            p.setNombre(e.getNombre());
            p.setPrecio(e.getPrecio());
            p.setCategoria(e.getCategoria());
            return sv.create(p);
        }).map(p -> ResponseEntity.created(URI.create("/productos/" + p.getId())).body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
        return sv.get(id).flatMap(p -> {
            p.setFile(
                    UUID.randomUUID().toString() + file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
            return file.transferTo(new File(path + p.getFile())).then(sv.create(p));
        }).map(p -> ResponseEntity.ok(p)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
        return sv.get(id).flatMap(p -> {
            return sv.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

}
