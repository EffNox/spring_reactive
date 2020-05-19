package nr.rest;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import nr.rest.models.documents.Categoria;
import nr.rest.models.documents.Producto;
import nr.rest.models.services.SProducto;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ApiRest  implements CommandLineRunner{

	private @Autowired SProducto sv;
	private @Autowired ReactiveMongoTemplate mt;
	// private Logger lg = LoggerFactory.getLogger(WebFlux.class);
	public static void main(String[] args) {
		SpringApplication.run(ApiRest.class, args);
	}
	@Override
	public void run(String... args) throws Exception {
		mt.dropCollection("producto").subscribe();
		mt.dropCollection("categoria").subscribe();

		Categoria elec = new Categoria(null, "Electrónico");
		Categoria depo = new Categoria(null, "Deporte");
		Categoria comp = new Categoria(null, "Computación");
		Categoria mue = new Categoria(null, "Muebles");

		Flux .just(elec,depo,comp,mue)
		.flatMap(sv::createCategoria)
		.thenMany(
			Flux.just(
					new Producto("Sony Camara HD Digital", 177.89, elec),
					 new Producto("Apple iPod", 46.89, elec),
					new Producto("Sony Notebook", 846.89, comp),
					new Producto("Hewlett Packard Multifuncional", 200.89, comp),
					new Producto("Bianchi Bicicleta", 70.89, depo),
					 new Producto("HP Notebook Omen 17", 2500.89, comp),
					new Producto("Mica Cómoda 5 Cajones", 150.89, mue),
					new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, elec)).flatMap(p -> {
						p.setCreateAt(new Date());
						return sv.create(p);
					})
		).subscribe();
	}

}
