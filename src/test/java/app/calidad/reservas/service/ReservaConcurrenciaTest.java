package app.calidad.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;

import app.calidad.reservas.dto.ReservaRequest;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.TipoRecurso;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.exception.ReservaSolapadaException;
import app.calidad.reservas.repository.RecursoRepository;
import app.calidad.reservas.repository.ReservaRepository;
import app.calidad.reservas.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:reservasconc;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class ReservaConcurrenciaTest {

    private static final int HILOS = 10;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    void reservasConcurrentesSolapadasSoloUnaPrevalece() throws Exception {
        Usuario usuario = usuarioRepository.save(
                new Usuario("Conc Test", "TEST-CONC", "conc@example.com", EstadoUsuario.ACTIVO));
        Recurso recurso = recursoRepository.save(
                new Recurso("TEST-Conc", "Sala de concurrencia", TipoRecurso.SALA_REUNIONES, 5,
                        EstadoRecurso.DISPONIBLE));
        LocalDateTime inicio = LocalDateTime.now().plusDays(60).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusHours(2);

        ExecutorService pool = Executors.newFixedThreadPool(HILOS);
        CountDownLatch listos = new CountDownLatch(HILOS);
        CountDownLatch disparo = new CountDownLatch(1);
        AtomicInteger creadas = new AtomicInteger();
        List<Throwable> fallos = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < HILOS; i++) {
            pool.submit(() -> {
                listos.countDown();
                try {
                    if (!disparo.await(10, TimeUnit.SECONDS)) {
                        fallos.add(new IllegalStateException("sin disparo"));
                        return;
                    }
                    reservaService.crearReserva(new ReservaRequest(usuario.getId(), recurso.getId(), inicio, fin));
                    creadas.incrementAndGet();
                } catch (Throwable t) {
                    fallos.add(t);
                }
            });
        }
        try {
            assertThat(listos.await(30, TimeUnit.SECONDS)).isTrue();
            disparo.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

            assertThat(creadas.get()).isEqualTo(1);
            assertThat(fallos).hasSize(HILOS - 1);
            assertThat(fallos).allMatch(f -> f instanceof ReservaSolapadaException);
        } finally {
            reservaRepository.findByRecursoId(recurso.getId()).forEach(reservaRepository::delete);
            usuarioRepository.delete(usuario);
            recursoRepository.delete(recurso);
        }
    }
}
