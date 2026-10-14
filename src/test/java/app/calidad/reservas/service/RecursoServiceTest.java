package app.calidad.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.calidad.reservas.dto.CambioEstadoRequest;
import app.calidad.reservas.dto.DisponibilidadResponse;
import app.calidad.reservas.dto.RecursoRequest;
import app.calidad.reservas.dto.RecursoResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.Reserva;
import app.calidad.reservas.entity.TipoRecurso;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.exception.NoEncontradoException;
import app.calidad.reservas.exception.ValidacionException;
import app.calidad.reservas.repository.RecursoRepository;
import app.calidad.reservas.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecursoServiceTest {

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private RecursoService recursoService;

    private Recurso recursoDisponible() {
        return new Recurso("Lab 101", "Laboratorio", TipoRecurso.LABORATORIO, 30, EstadoRecurso.DISPONIBLE);
    }

    private Reserva reservaActiva(Recurso recurso, LocalDateTime inicio, LocalDateTime fin) {
        Usuario usuario = new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO);
        return new Reserva(usuario, recurso, inicio, fin, EstadoReserva.ACTIVA);
    }

    @Test
    void crearRecursoDebePersistirConEstadoDisponible() {
        RecursoRequest request = new RecursoRequest("Lab 102", "Nuevo lab", TipoRecurso.LABORATORIO, 25);
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        RecursoResponse resultado = recursoService.crearRecurso(request);

        assertThat(resultado.nombre()).isEqualTo("Lab 102");
        assertThat(resultado.tipo()).isEqualTo(TipoRecurso.LABORATORIO);
        assertThat(resultado.capacidad()).isEqualTo(25);
        assertThat(resultado.estado()).isEqualTo(EstadoRecurso.DISPONIBLE);
    }

    @Test
    void listarRecursosSinFiltrosDebeRetornarTodo() {
        when(recursoRepository.findAll()).thenReturn(List.of(recursoDisponible()));

        assertThat(recursoService.listarRecursos(null, null)).hasSize(1);
    }

    @Test
    void listarRecursosConEstadoDebeFiltrarPorEstado() {
        when(recursoRepository.findByEstado(EstadoRecurso.MANTENIMIENTO)).thenReturn(List.of());

        assertThat(recursoService.listarRecursos(EstadoRecurso.MANTENIMIENTO, null)).isEmpty();
    }

    @Test
    void listarRecursosConTipoDebeFiltrarPorTipo() {
        when(recursoRepository.findByTipo(TipoRecurso.AUDITORIO)).thenReturn(List.of(recursoDisponible()));

        assertThat(recursoService.listarRecursos(null, TipoRecurso.AUDITORIO)).hasSize(1);
    }

    @Test
    void listarRecursosConEstadoYTipoDebeFiltrarPorAmbos() {
        when(recursoRepository.findByEstadoAndTipo(EstadoRecurso.DISPONIBLE, TipoRecurso.LABORATORIO))
                .thenReturn(List.of(recursoDisponible()));

        assertThat(recursoService.listarRecursos(EstadoRecurso.DISPONIBLE, TipoRecurso.LABORATORIO)).hasSize(1);
    }

    @Test
    void obtenerPorIdDebeRetornarElRecurso() {
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recursoDisponible()));

        RecursoResponse resultado = recursoService.obtenerPorId(1L);

        assertThat(resultado.nombre()).isEqualTo("Lab 101");
    }

    @Test
    void obtenerPorIdDebeFallarCuandoNoExiste() {
        when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recursoService.obtenerPorId(999L))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void actualizarRecursoDebeModificarTodosLosCampos() {
        Recurso recurso = recursoDisponible();
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        RecursoResponse resultado = recursoService.actualizarRecurso(
                1L, new RecursoRequest("Lab 200", "Remodelado", TipoRecurso.AUDITORIO, 100));

        assertThat(resultado.nombre()).isEqualTo("Lab 200");
        assertThat(resultado.descripcion()).isEqualTo("Remodelado");
        assertThat(resultado.tipo()).isEqualTo(TipoRecurso.AUDITORIO);
        assertThat(resultado.capacidad()).isEqualTo(100);
        assertThat(resultado.estado()).isEqualTo(EstadoRecurso.DISPONIBLE);
    }

    @Test
    void actualizarRecursoDebeFallarCuandoNoExiste() {
        when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recursoService.actualizarRecurso(
                999L, new RecursoRequest("X", null, TipoRecurso.OTRO, 1)))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void cambiarEstadoDebeActualizarElEstado() {
        Recurso recurso = recursoDisponible();
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        RecursoResponse resultado = recursoService.cambiarEstado(1L, new CambioEstadoRequest("MANTENIMIENTO"));

        assertThat(resultado.estado()).isEqualTo(EstadoRecurso.MANTENIMIENTO);
    }

    @Test
    void cambiarEstadoDebeFallarCuandoNoExiste() {
        when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recursoService.cambiarEstado(999L, new CambioEstadoRequest("MANTENIMIENTO")))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void cambiarEstadoDebeFallarCuandoEstadoEsInvalido() {
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recursoDisponible()));

        assertThatThrownBy(() -> recursoService.cambiarEstado(1L, new CambioEstadoRequest("ROTO")))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void disponibilidadEnMantenimientoDebeSerFalse() {
        Recurso recurso = new Recurso("Auditorio", null, TipoRecurso.AUDITORIO, 200, EstadoRecurso.MANTENIMIENTO);
        when(recursoRepository.findById(2L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.findByRecursoIdAndEstado(2L, EstadoReserva.ACTIVA)).thenReturn(List.of());
        LocalDateTime desde = LocalDateTime.of(2026, 10, 20, 9, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 10, 20, 11, 0, 0);

        DisponibilidadResponse resultado = recursoService.consultarDisponibilidad(2L, desde, hasta);

        assertThat(resultado.disponible()).isFalse();
        assertThat(resultado.reservasSolapadas()).isEmpty();
    }

    @Test
    void disponibilidadConReservaSolapadaDebeSerFalseYListarla() {
        Recurso recurso = recursoDisponible();
        Reserva solapada = reservaActiva(recurso,
                LocalDateTime.of(2026, 10, 20, 9, 0, 0), LocalDateTime.of(2026, 10, 20, 11, 0, 0));
        Reserva fueraDeRango = reservaActiva(recurso,
                LocalDateTime.of(2026, 10, 21, 9, 0, 0), LocalDateTime.of(2026, 10, 21, 11, 0, 0));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.findByRecursoIdAndEstado(1L, EstadoReserva.ACTIVA))
                .thenReturn(List.of(solapada, fueraDeRango));

        DisponibilidadResponse resultado = recursoService.consultarDisponibilidad(1L,
                LocalDateTime.of(2026, 10, 20, 10, 0, 0), LocalDateTime.of(2026, 10, 20, 12, 0, 0));

        assertThat(resultado.disponible()).isFalse();
        assertThat(resultado.reservasSolapadas()).hasSize(1);
    }

    @Test
    void disponibilidadEnRangoLibreDebeSerTrue() {
        Recurso recurso = recursoDisponible();
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.findByRecursoIdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(List.of());

        DisponibilidadResponse resultado = recursoService.consultarDisponibilidad(1L,
                LocalDateTime.of(2026, 10, 20, 12, 0, 0), LocalDateTime.of(2026, 10, 20, 14, 0, 0));

        assertThat(resultado.recursoId()).isEqualTo(1L);
        assertThat(resultado.disponible()).isTrue();
        assertThat(resultado.reservasSolapadas()).isEmpty();
    }

    @Test
    void disponibilidadDebeFallarCuandoNoExisteElRecurso() {
        when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recursoService.consultarDisponibilidad(999L,
                LocalDateTime.of(2026, 10, 20, 9, 0, 0), LocalDateTime.of(2026, 10, 20, 11, 0, 0)))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void disponibilidadDebeFallarCuandoRangoEsInvalido() {
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recursoDisponible()));

        assertThatThrownBy(() -> recursoService.consultarDisponibilidad(1L,
                LocalDateTime.of(2026, 10, 20, 11, 0, 0), LocalDateTime.of(2026, 10, 20, 9, 0, 0)))
                .isInstanceOf(ValidacionException.class);
    }
}
