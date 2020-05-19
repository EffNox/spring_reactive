package nr.reactor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import nr.reactor.models.Comentarios;
import nr.reactor.models.UsuConCom;
import nr.reactor.models.Usuario;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class AppReactor implements CommandLineRunner {

	Logger lg = LoggerFactory.getLogger(AppReactor.class);

	public static void main(String[] args) {
		SpringApplication.run(AppReactor.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// exampleIterable();
		// exampleFlatMap();
		// exampleToString();
		// exampleToCollectList();
		// exampleUsuComFlatMap();
		// exampleUsuComZipWith();
		// exampleUsuComZipWith2();
		// exampleZipWithRanges();
		// exampleInterval();
		// exampleDelayElements();
		// exampleIntervalInfinite();
		// exampleIntervalFromCreate();
		exampleContraPresion();
	}

	public void exampleIterable(String... args) throws Exception {
		List<String> users = new ArrayList<>();
		users.add("Andres Mel");
		users.add("Fernando Ticona");
		users.add("s J");
		users.add("Ximena Sanchez");
		users.add("Juan Mendoza");
		users.add("Bruce Lee");
		users.add("Bruce Wills");

		Flux<String> nombres = Flux
				.fromIterable(users); /*
										 * Flux.just("Andres Mel", "Fernando Ticona", "s J", "Ximena Sanchez",
										 * "Juan Mendoza", "Bruce Lee", "Bruce Wills");
										 */

		Flux<Usuario> usuarios = nombres
				.map(nom -> new Usuario(nom.split(" ")[0].toUpperCase(), nom.split(" ")[1].toUpperCase()))
				.filter(user -> user.getNombre().equalsIgnoreCase("bruce")).doOnNext(user -> {
					if (user == null) {
						throw new RuntimeException("Nombre no pueden se vacio");
					}
					System.out.println(user.getNombre().concat(" ").concat(user.getApellido()));
				}).map(user -> {
					String nom = user.getNombre().toLowerCase();
					user.setNombre(nom);
					return user;
				});

		usuarios.subscribe(e -> lg.info(e.toString()), er -> lg.error(er.getMessage()), new Runnable() {
			@Override
			public void run() {
				lg.info("Ha finalizado la ejecución del observable con éxito!");
			}
		});
	}

	public void exampleFlatMap(String... args) throws Exception {
		List<String> users = new ArrayList<>();
		users.add("Andres Mel");
		users.add("Fernando Ticona");
		users.add("s J");
		users.add("Ximena Sanchez");
		users.add("Juan Mendoza");
		users.add("Bruce Lee");
		users.add("Bruce Wills");

		Flux.fromIterable(users)
				.map(nom -> new Usuario(nom.split(" ")[0].toUpperCase(), nom.split(" ")[1].toUpperCase()))
				.flatMap(user -> {
					return user.getNombre().equalsIgnoreCase("bruce") ? Mono.just(user) : Mono.empty();
				}).map(user -> {
					String nom = user.getNombre().toLowerCase();
					user.setNombre(nom);
					return user;
				}).subscribe(u -> lg.info(u.toString()));
	}

	public void exampleToString(String... args) throws Exception {
		List<Usuario> users = new ArrayList<>();
		users.add(new Usuario("Andres", "Mel"));
		users.add(new Usuario("Fernando", "Ticona"));
		users.add(new Usuario("s", "J"));
		users.add(new Usuario("Ximena", "Sanchez"));
		users.add(new Usuario("Juan", "Mendoza"));
		users.add(new Usuario("Bruce", "Lee"));
		users.add(new Usuario("Bruce", "Wills"));

		Flux.fromIterable(users)
				.map(user -> user.getNombre().toUpperCase().concat(" ").concat(user.getApellido().toUpperCase()))
				.flatMap(nom -> nom.contains("bruce".toUpperCase()) ? Mono.just(nom) : Mono.empty())
				.map(nom -> nom.toLowerCase()).subscribe(lg::info);
	}

	public void exampleToCollectList(String... args) throws Exception {
		List<Usuario> users = new ArrayList<>();
		users.add(new Usuario("Andres", "Mel"));
		users.add(new Usuario("Fernando", "Ticona"));
		users.add(new Usuario("s", "J"));
		users.add(new Usuario("Ximena", "Sanchez"));
		users.add(new Usuario("Juan", "Mendoza"));
		users.add(new Usuario("Bruce", "Lee"));
		users.add(new Usuario("Bruce", "Wills"));

		Flux.fromIterable(users).collectList().subscribe(lst -> lst.forEach(item -> lg.info(item.toString())));

	}

	public void exampleUsuComFlatMap() {
		Mono<Usuario> usuMom = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comMon = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola, pepe, qué tal!!");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		usuMom.flatMap(u -> comMon.map(c -> new UsuConCom(u, c))).subscribe(uc -> {
			lg.info(uc.toString());
		});

	}

	public void exampleUsuComZipWith() {
		Mono<Usuario> usuMom = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comUsuMon = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola, pepe, qué tal!!");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		Mono<UsuConCom> ucMono = usuMom.zipWith(comUsuMon, (u, comUsu) -> new UsuConCom(u, comUsu));
		ucMono.subscribe(uc -> {
			lg.info(uc.toString());
		});

	}

	public void exampleUsuComZipWith2() {
		Mono<Usuario> usuMom = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comUsuMon = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola, pepe, qué tal!!");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuConCom> ucMono = usuMom.zipWith(comUsuMon).map(tuple -> {
			Usuario u = tuple.getT1();
			Comentarios c = tuple.getT2();
			return new UsuConCom(u, c);
		});

		ucMono.subscribe(uc -> lg.info(uc.toString()));

	}

	public void exampleZipWithRanges() {
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4).map(i -> (i * 2))
				.zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(lg::info);
	}

	public void exampleInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		rango.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> lg.info(i.toString())).blockLast();
	}

	public void exampleDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> lg.info(i.toString()));
		rango.blockLast();
	}

	public void exampleIntervalInfinite() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown)
				.flatMap(i -> (i >= 5) ? Flux.error(new InterruptedException("Solo hasta5!")) : Flux.just(i))
				.map(i -> "Hola: " + i).retry(2).subscribe(s -> lg.info(s), e -> lg.error(e.getMessage()));
		latch.await();
	}

	public void exampleIntervalFromCreate() throws InterruptedException {
		Flux.create(emitter -> {
			Timer tm = new Timer();
			tm.schedule(new TimerTask() {
				private Integer count = 0;

				@Override
				public void run() {
					emitter.next(++count);
					if (count == 10) {
						tm.cancel();
						emitter.complete();
					}
					if (count == 5) {
						tm.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
				// .doOnNext(e->lg.info(e.toString()))
				// .doOnComplete(()-> lg.info("Temrino"))
				.subscribe(nx -> lg.info(nx.toString()), er -> lg.info(er.getMessage()), () -> lg.info("Temrino"));
	}

	private void exampleContraPresion() {
		Flux.range(1, 10)
		.log()
		.limitRate(5)
		.subscribe(/*new Subscriber<Integer>() {

			private Subscription s;
			private Integer limite = 5;
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
			}

			@Override
			public void onNext(Integer t) {
				lg.info(t.toString());
				consumido++;
				if (consumido==limite) {
					consumido=0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {}

			@Override
			public void onComplete() {}

		}*/);
	}
}
