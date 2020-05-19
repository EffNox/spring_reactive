package nr.rest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import nr.rest.models.documents.Categoria;
import nr.rest.models.documents.Producto;
import nr.rest.models.services.SProducto;
import reactor.core.publisher.Mono;

// @AutoConfigureWebTestClient //MOCK
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	private @Autowired WebTestClient cliente;
	private @Autowired SProducto sv;
	private @Value("${config.endpoint.path}") String url;

	@Test
	void getAll() {
		cliente.get().uri(url)
		.accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus().isOk()
				.expectHeader()
				.contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
				.consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
					productos.forEach(p -> System.out.println(p.getNombre()));
					Assertions.assertThat(productos.size() > 0).isTrue();
				});
		// .hasSize(8);
	}

	@Test
	void get() {
		Producto p=sv.getByNombre("Apple iPod").block();
		cliente.get().uri(url+"/{id}", Collections.singletonMap("id", p.getId()))
		.accept(MediaType.APPLICATION_JSON).exchange()
		.expectStatus().isOk()
				.expectHeader()
				.contentType(MediaType.APPLICATION_JSON)
				.expectBody(Producto.class).consumeWith(response -> {
					Producto producto = response.getResponseBody();
					Assertions.assertThat(producto.getId()).isNotEmpty();
					Assertions.assertThat(producto.getId().length()>0).isTrue();
					Assertions.assertThat(producto.getNombre()).isEqualTo("Apple iPod");
				})
				/* .expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Apple iPod") */;
	}

	@Test
	public void create() {
		Categoria categoria = sv.findCategoriaByNombre("Muebles").block();
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		cliente.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto),Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		// ESTE FUNCIONA CON LOS HANDLER "/v2/productos" en el property
		// .jsonPath("$.id").isNotEmpty()
		// .jsonPath("$.nombre").isEqualTo("Mesa comedor")
		// .jsonPath("$.categoria.nombre").isEqualTo("Muebles")
		.jsonPath("$.dt.id").isNotEmpty()
		.jsonPath("$.dt.nombre").isEqualTo("Mesa comedor")
		.jsonPath("$.dt.categoria.nombre").isEqualTo("Muebles");

	}
// ESTE FUNCIONA CON LOS HANDLER "/v2/productos" en el property
	// @Test
	// public void create2() {
	// 	Categoria categoria = sv.findCategoriaByNombre("Muebles").block();
	// 	Producto producto = new Producto("Mesa comedor", 100.00, categoria);
	// 	cliente.post().uri(url)
	// 	.contentType(MediaType.APPLICATION_JSON)
	// 	.accept(MediaType.APPLICATION_JSON)
	// 	.body(Mono.just(producto),Producto.class)
	// 	.exchange()
	// 	.expectStatus().isCreated()
	// 	.expectHeader().contentType(MediaType.APPLICATION_JSON)
	// 	.expectBody(Producto.class)
	// 	.consumeWith(rs->{
	// 		Producto p = rs.getResponseBody();
	// 		Assertions.assertThat(p.getId()).isNotEmpty();
	// 		Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
	// 		Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
	// 	});
	// }


	@Test
	public void create2() {
		Categoria categoria = sv.findCategoriaByNombre("Muebles").block();
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		cliente.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto),Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		.consumeWith(rs->{
			Object o = rs.getResponseBody().get("dt");
			Producto p = new ObjectMapper().convertValue(o, Producto.class);
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
			Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
		});
	}

	@Test
	public void update() {
		Producto p = sv.getByNombre("Sony Notebook").block();
		Categoria categoria = sv.findCategoriaByNombre("Electrónico").block();

		Producto productoEdit = new Producto("Producto Editado", 22.2, categoria);

		cliente.put().uri(url+"/{id}",Collections.singletonMap("id", p.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(productoEdit),Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Producto Editado")
		.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
	}

	@Test
	public void delete() {
		Producto p = sv.getByNombre("Mica Cómoda 5 Cajones").block();
		cliente.delete().uri(url+"/{id}",Collections.singletonMap("id", p.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();

		cliente.get().uri("/v2/productos/{id}",Collections.singletonMap("id", p.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody().isEmpty();
	}
}
