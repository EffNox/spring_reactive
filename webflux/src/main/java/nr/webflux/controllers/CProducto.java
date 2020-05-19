package nr.webflux.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import nr.webflux.models.documents.Categoria;
import nr.webflux.models.documents.Producto;
import nr.webflux.models.services.SProducto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
@RequestMapping("/producto")
public class CProducto {

    private @Value("${config.uploads.path}") String UPLOAS;
    private @Autowired SProducto sv;
    private Logger lg = LoggerFactory.getLogger(CProducto.class);

    @ModelAttribute("categorias")
    public Flux<Categoria> getAllCategoria() {
        return sv.getAllCategoria();
    }

    @GetMapping("/uploads/img/{nomFoto:.+}")
    public Mono<ResponseEntity<UrlResource>> verFoto(@PathVariable String nomFoto) throws MalformedURLException {
        Path ruta = Paths.get(UPLOAS).resolve(nomFoto).toAbsolutePath();
        UrlResource img = new UrlResource(ruta.toUri());
		return Mono.just(
				ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFoto + "\"")
				.body(img)
				);
    }


    @GetMapping
    public Mono<String> getAll(Model m) {
        Flux<Producto> p = sv.getAllNombreUpperCase();
        p.subscribe(pr -> lg.info(pr.toString()));
        m.addAttribute("productos", p);
        m.addAttribute("titulo", "Litado de Productos");
        return Mono.just("index");
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model m, @PathVariable String id) {
      return  sv.get(id).doOnNext(p->{
            m.addAttribute("producto", p);
            m.addAttribute("titulo", "Detalle del Productos");
        }).switchIfEmpty(Mono.just(new Producto()))
        .flatMap(p->{
            if (p.getId()==null) {
                return Mono.error(new InterruptedException("No existe el producto"));
            }
            return Mono.just(p);
        }).then(Mono.just("ver"))
        .onErrorResume(ex -> Mono.just("redirect:/producto?error=no+existe+el+producto"));
    }
    
    @GetMapping("/form")
    public Mono<String> create(Model m) {
        m.addAttribute("producto", new Producto());
        m.addAttribute("titulo", "Formulario de Productos");
        return Mono.just("form");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model m) {
        Mono<Producto> pMono = sv.get(id).doOnNext(p -> lg.info(p.toString())).defaultIfEmpty(new Producto());
        m.addAttribute("producto", pMono);
        m.addAttribute("titulo", "Editar Producto");
        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2(@PathVariable String id, Model m) {
        return sv.get(id).doOnNext(p -> {
            // lg.info(p.toString())
            m.addAttribute("producto", p);
            m.addAttribute("titulo", "Editar Producto");
        }).defaultIfEmpty(new Producto()).flatMap(p -> {
            if (p.getId() == null) {
                return Mono.error(new InterruptedException("No existe el producto"));
            }
            return Mono.just(p);
        }).thenReturn("form").onErrorResume(ex -> Mono.just("redirect:/producto?error=no+existe+el+producto"));
    }

    @PostMapping("/form")
    public Mono<String> save(Producto e, SessionStatus st, @RequestPart(name = "file2") FilePart file) {
        st.setComplete();
        Mono<Categoria> cat = sv.getCategoria(e.getCategoria().getId());
        return cat.flatMap(c -> {
            if (!file.filename().isEmpty()) {
                e.setFile(UUID.randomUUID().toString() + "_"
                        + file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
            }
            e.setCategoria(c);
            return sv.create(e);
        }).doOnNext(pr -> lg.info("Insert: " + pr.toString()))
        .flatMap(p -> {
            if (!file.filename().isEmpty()) {
                return file.transferTo(new File(UPLOAS + p.getFile()));
            }
            return Mono.empty();
        }).thenReturn("redirect:/producto?success=producto+guardado+con+exito");
    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return sv.get(id).defaultIfEmpty(new Producto())
                .flatMap(p -> (p.getId() == null) ? Mono.error(new InterruptedException("No existe el producto"))
                        : Mono.just(p))
                .flatMap(sv::delete).then(Mono.just("redirect:/producto?success=producto+eliminado+con+exito"))
                .onErrorResume(ex -> Mono.just("redirect:/producto?error=no+existe+el+producto+a+eliminar"));
    }

    @GetMapping("/driver")
    public String getAllDriver(Model m) {
        Flux<Producto> p = sv.getAllNombreUpperCase().delayElements(Duration.ofSeconds(1));
        p.subscribe(pr -> lg.info(pr.toString()));
        m.addAttribute("productos", new ReactiveDataDriverContextVariable(p, 2));
        m.addAttribute("titulo", "Litado de Productos");
        return "index";
    }

    @GetMapping("/full")
    public String getAllFull(Model m) {
        Flux<Producto> p = sv.getAllNombreUpperCaseRepeat();
        m.addAttribute("productos", p);
        m.addAttribute("titulo", "Litado de Productos");
        return "index";
    }

    @GetMapping("/chunked")
    public String getAllChunked(Model m) {
        Flux<Producto> p = sv.getAllNombreUpperCaseRepeat();
        m.addAttribute("productos", p);
        m.addAttribute("titulo", "Litado de Productos");
        return "chunked";
    }

}
