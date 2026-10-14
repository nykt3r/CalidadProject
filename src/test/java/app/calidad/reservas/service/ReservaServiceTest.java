package app.calidad.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import app.calidad.reservas.dto.ReservaRequest;
import app.calidad.reservas.dto.ReservaResponse;
import app.calidad.reservas.entity.EstadoRecurso;
import app.calidad.reservas.entity.EstadoReserva;
import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Recurso;
import app.calidad.reservas.entity.Reserva;
import app.calidad.reservas.entity.TipoRecurso;
import app.calidad.reservas.entity.Usuario;
import app.calidad.reservas.exception.LimiteReservasException;
import app.calidad.reservas.exception.NoEncontradoException;
import app.calidad.reservas.exception.RecursoEnMantenimientoException;
import app.calidad.reservas.exception.RecursoFueraDeServicioException;
import app.calidad.reservas.exception.ReservaFinalizadaException;
import app.calidad.reservas.exception.ReservaSolapadaException;
import app.calidad.reservas.exception.UsuarioInactivoException;
import app.calidad.reservas.exception.ValidacionException;
import app.calidad.reservas.repository.RecursoRepository;
import app.calidad.reservas.repository.ReservaRepository;
import app.calidad.reservas.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario usuarioActivo() {
        return new Usuario("Ana García", "DOC-001", "ana@example.com", EstadoUsuario.ACTIVO);
    }

    private Recurso recursoDisponible() {
        return new Recurso("Lab 101", "Laboratorio", TipoRecurso.LABORATORIO, 30, EstadoRecurso.DISPONIBLE);
    }

    private LocalDateTime futuro(int dias, int horaInicio, int horaFin) {
        return LocalDateTime.now().plusDays(dias).withHour(horaInicio).withMinute(0).withSecond(0).withNano(0);
    }

    private void mockBaseOk(Usuario usuario, Recurso recurso, long activas) {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(any(), eq(EstadoReserva.ACTIVA))).thenReturn(activas);
        when(reservaRepository.findActivasSolapadas(any(), any(), any())).thenReturn(List.of());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crearReservaDebePersistirEnEstadoActiva() {
        Usuario usuario = usuarioActivo();
        Recurso recurso = recursoDisponible();
        mockBaseOk(usuario, recurso, 2L);
        LocalDateTime inicio = futuro(10, 9, 0);
        LocalDateTime fin = futuro(10, 11, 0);

        ReservaResponse resultado = reservaService.crearReserva(new ReservaRequest(1L, 1L, inicio, fin));

        assertThat(resultado.estado()).isEqualTo(EstadoReserva.ACTIVA);
        assertThat(resultado.fechaInicio()).isEqualTo(inicio);
        assertThat(resultado.usuario().nombre()).isEqualTo("Ana García");
        assertThat(resultado.recurso().nombre()).isEqualTo("Lab 101");
    }

    @Test
    void crearReservaDebeFallarCuandoUsuarioNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(999L, 1L, futuro(10, 9, 0), futuro(10, 11, 0))))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoRecursoNoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo()));
        when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(1L, 999L, futuro(10, 9, 0), futuro(10, 11, 0))))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoFinNoEsPosteriorAlInicioRegla4() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo()));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recursoDisponible()));
        LocalDateTime inicio = futuro(10, 11, 0);

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(1L, 1L, inicio, inicio)))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoInicioEsPasadoRegla7() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo()));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recursoDisponible()));

        assertThatThrownBy(() -> reservaService.crearReserva(
                new ReservaRequest(1L, 1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoUsuarioInactivoRegla1() {
        Usuario inactivo = new Usuario("Luis Pérez", "DOC-002", "luis@example.com", EstadoUsuario.INACTIVO);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(inactivo));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recursoDisponible()));

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(2L, 1L, futuro(10, 9, 0), futuro(10, 11, 0))))
                .isInstanceOf(UsuarioInactivoException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoRecursoEnMantenimientoRegla2() {
        Recurso mantenimiento = new Recurso("Auditorio", null, TipoRecurso.AUDITORIO, 200, EstadoRecurso.MANTENIMIENTO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo()));
        when(recursoRepository.findById(2L)).thenReturn(Optional.of(mantenimiento));

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(1L, 2L, futuro(10, 9, 0), futuro(10, 11, 0))))
                .isInstanceOf(RecursoEnMantenimientoException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoRecursoFueraDeServicioRegla3() {
        Recurso fuera = new Recurso("Sala", null, TipoRecurso.SALA_REUNIONES, 10, EstadoRecurso.FUERA_DE_SERVICIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo()));
        when(recursoRepository.findById(3L)).thenReturn(Optional.of(fuera));

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(1L, 3L, futuro(10, 9, 0), futuro(10, 11, 0))))
                .isInstanceOf(RecursoFueraDeServicioException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoSuperaTresActivasRegla8() {
        Usuario usuario = usuarioActivo();
        Recurso recurso = recursoDisponible();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(any(), eq(EstadoReserva.ACTIVA))).thenReturn(3L);

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(1L, 1L, futuro(10, 9, 0), futuro(10, 11, 0))))
                .isInstanceOf(LimiteReservasException.class);
    }

    @Test
    void crearReservaDebeFallarCuandoExisteSolapamientoRegla5() {
        Usuario usuario = usuarioActivo();
        Recurso recurso = recursoDisponible();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(any(), eq(EstadoReserva.ACTIVA))).thenReturn(0L);
        Reserva existente = new Reserva(usuario, recurso, futuro(10, 9, 0), futuro(10, 11, 0), EstadoReserva.ACTIVA);
        when(reservaRepository.findActivasSolapadas(any(), any(), any())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> reservaService.crearReserva(new ReservaRequest(1L, 1L, futuro(10, 10, 0), futuro(10, 12, 0))))
                .isInstanceOf(ReservaSolapadaException.class);
    }

    @Test
    void cancelarReservaDebePasarACanceladaRegla6() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse resultado = reservaService.cancelarReserva(1L);

        assertThat(resultado.estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void cancelarReservaDebeFallarCuandoEstaFinalizadaRegla6() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.FINALIZADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1L))
                .isInstanceOf(ReservaFinalizadaException.class);
    }

    @Test
    void cancelarReservaDebeFallarCuandoYaEstaCanceladaRegla6() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.CANCELADA);
        when(reservaRepository.findById(2L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(2L))
                .isInstanceOf(ReservaFinalizadaException.class);
    }

    @Test
    void cancelarReservaDebeFallarCuandoNoExiste() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.cancelarReserva(999L))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void obtenerPorIdDebeRetornarLaReserva() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThat(reservaService.obtenerPorId(1L).estado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void obtenerPorIdDebeFallarCuandoNoExiste() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.obtenerPorId(999L))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void listarPorUsuarioSinFiltroDebeRetornarTodas() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        when(reservaRepository.findByUsuarioId(1L)).thenReturn(List.of(reserva));

        assertThat(reservaService.listarPorUsuario(1L, null)).hasSize(1);
    }

    @Test
    void listarPorUsuarioConEstadoDebeFiltrar() {
        when(reservaRepository.findByUsuarioIdAndEstado(1L, EstadoReserva.CANCELADA)).thenReturn(List.of());

        assertThat(reservaService.listarPorUsuario(1L, EstadoReserva.CANCELADA)).isEmpty();
    }

    @Test
    void listarPorRecursoSinFiltrosDebeRetornarTodas() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        when(reservaRepository.findByRecursoId(1L)).thenReturn(List.of(reserva));

        assertThat(reservaService.listarPorRecurso(1L, null, null)).hasSize(1);
    }

    @Test
    void listarPorRecursoEnRangoDebeUsarQuery() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        LocalDateTime desde = futuro(10, 8, 0);
        LocalDateTime hasta = futuro(10, 12, 0);
        when(reservaRepository.findPorRecursoEnRango(1L, desde, hasta)).thenReturn(List.of(reserva));

        assertThat(reservaService.listarPorRecurso(1L, desde, hasta)).hasSize(1);
    }

    @Test
    void listarPorRecursoEnRangoInvalidoDebeFallar() {
        LocalDateTime desde = futuro(10, 12, 0);
        LocalDateTime hasta = futuro(10, 8, 0);

        assertThatThrownBy(() -> reservaService.listarPorRecurso(1L, desde, hasta))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void listarPorRecursoSoloDesdeDebeFiltrarHaciaAdelante() {
        Reserva futura = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        Reserva pasada = new Reserva(usuarioActivo(), recursoDisponible(),
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(5).plusHours(2), EstadoReserva.FINALIZADA);
        when(reservaRepository.findByRecursoId(1L)).thenReturn(List.of(futura, pasada));

        assertThat(reservaService.listarPorRecurso(1L, LocalDateTime.now(), null)).hasSize(1);
    }

    @Test
    void listarPorRecursoSoloHastaDebeFiltrarHaciaAtras() {
        Reserva futura = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        Reserva pasada = new Reserva(usuarioActivo(), recursoDisponible(),
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(5).plusHours(2), EstadoReserva.FINALIZADA);
        when(reservaRepository.findByRecursoId(1L)).thenReturn(List.of(futura, pasada));

        assertThat(reservaService.listarPorRecurso(1L, null, LocalDateTime.now())).hasSize(1);
    }

    @Test
    void listarPorFechaDebeRetornarLasQueIntersectanElDia() {
        Reserva reserva = new Reserva(usuarioActivo(), recursoDisponible(), futuro(10, 9, 0), futuro(10, 11, 0),
                EstadoReserva.ACTIVA);
        when(reservaRepository.findEnRango(any(), any())).thenReturn(List.of(reserva));

        assertThat(reservaService.listarPorFecha(LocalDate.now().plusDays(10))).hasSize(1);
    }
}
