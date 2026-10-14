package steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import app.calidad.reservas.repository.ReservaRepository;

public class reservaSteps {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @LocalServerPort
    private int port;

    @Autowired
    private ReservaRepository reservaRepository;

    private EntityExchangeResult<String> respuesta;

    private Long reservaCreadaId;

    private Long usuarioRegistradoId;

    private RestTestClient client() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    private void limpiar() {
        reservaRepository.findAll().stream()
                .filter(r -> !r.getId().equals(1L))
                .forEach(reservaRepository::delete);
        reservaCreadaId = null;
        usuarioRegistradoId = null;
    }

    @Before(order = 1)
    public void limpiarAntes() {
        limpiar();
    }

    @After(order = 20000)
    public void limpiarDespues() {
        limpiar();
    }

    private void guardarRespuesta(EntityExchangeResult<String> resultado) {
        respuesta = resultado;
        if (respuesta.getStatus() == HttpStatus.CREATED) {
            Number id = JsonPath.read(respuesta.getResponseBody(), "$.id");
            reservaCreadaId = id.longValue();
        }
    }

    private String[] slotFuturo(int numero) {
        LocalDateTime inicio = LocalDateTime.now().plusDays(30)
                .withHour(7 + numero * 2).withMinute(0).withSecond(0).withNano(0);
        return new String[]{inicio.format(FMT), inicio.plusHours(1).format(FMT)};
    }

    @When("a client books a future slot {int} on resource {int} for user {int}")
    public void unClienteReservaUnSlotFuturo(int slot, int recursoId, int usuarioId) {
        String[] rango = slotFuturo(slot);
        reservar(recursoId, (long) usuarioId, rango[0], rango[1]);
    }

    @When("a client books resource {int} for user {int} from {string} to {string}")
    public void unClienteReservaConRango(int recursoId, int usuarioId, String desde, String hasta) {
        reservar(recursoId, (long) usuarioId, desde, hasta);
    }

    @When("a client books a past slot on resource {int} for user {int}")
    public void unClienteReservaUnSlotPasado(int recursoId, int usuarioId) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        reservar(recursoId, (long) usuarioId, inicio.format(FMT), inicio.plusHours(2).format(FMT));
    }

    private void reservar(long recursoId, long usuarioId, String desde, String hasta) {
        String body = "{\"usuarioId\":" + usuarioId + ",\"recursoId\":" + recursoId + ","
                + "\"fechaInicio\":\"" + desde + "\",\"fechaFin\":\"" + hasta + "\"}";
        guardarRespuesta(client().post().uri("/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult());
    }

    @When("a client registers the booking user {string} with document {string} and email {string}")
    public void unClienteRegistraElUsuarioDeReserva(String nombre, String documento, String correo) {
        String body = "{\"nombre\":\"" + nombre + "\",\"documento\":\"" + documento + "\",\"correo\":\"" + correo + "\"}";
        EntityExchangeResult<String> resultado = client().post().uri("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
        Number id = JsonPath.read(resultado.getResponseBody(), "$.id");
        usuarioRegistradoId = id.longValue();
    }

    @When("a client books future slot {int} on resource {int} for the registered user")
    public void unClienteReservaSlotParaElUsuarioRegistrado(int slot, int recursoId) {
        String[] rango = slotFuturo(slot);
        reservar(recursoId, usuarioRegistradoId, rango[0], rango[1]);
    }

    @When("a client requests the created booking by id")
    public void unClienteConsultaLaReservaCreada() {
        respuesta = client().get().uri("/reservas/" + reservaCreadaId)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client requests the booking with id {int}")
    public void unClienteConsultaLaReservaPorId(int id) {
        respuesta = client().get().uri("/reservas/" + id)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client cancels the created booking")
    public void unClienteCancelaLaReservaCreada() {
        respuesta = client().delete().uri("/reservas/" + reservaCreadaId)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client cancels the booking with id {int}")
    public void unClienteCancelaLaReservaPorId(int id) {
        respuesta = client().delete().uri("/reservas/" + id)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client lists bookings of user {int}")
    public void unClienteListaReservasDelUsuario(int usuarioId) {
        respuesta = client().get().uri("/reservas/usuario/" + usuarioId)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client lists bookings of user {int} with state {string}")
    public void unClienteListaReservasDelUsuarioPorEstado(int usuarioId, String estado) {
        respuesta = client().get().uri("/reservas/usuario/" + usuarioId + "?estado=" + estado)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client lists bookings of resource {int}")
    public void unClienteListaReservasDelRecurso(int recursoId) {
        respuesta = client().get().uri("/reservas/recurso/" + recursoId)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client lists bookings of resource {int} from {string} to {string}")
    public void unClienteListaReservasDelRecursoEnRango(int recursoId, String desde, String hasta) {
        respuesta = client().get().uri("/reservas/recurso/" + recursoId + "?desde=" + desde + "&hasta=" + hasta)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client lists bookings on date {string}")
    public void unClienteListaReservasPorFecha(String fecha) {
        respuesta = client().get().uri("/reservas?fecha=" + fecha)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @Then("the booking response status must be {int}")
    public void elEstadoDeLaRespuestaDeReservaDebeSer(int estado) {
        assertThat(respuesta.getStatus().value()).isEqualTo(estado);
    }

    @Then("the booking field {string} must be {string}")
    public void elCampoDeLaReservaDebeSer(String campo, String valor) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$." + campo);
        assertThat(actual).isEqualTo(valor);
    }

    @Then("the booking error code must be {string}")
    public void elCodigoDeErrorDeReservaDebeSer(String codigo) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$.error");
        assertThat(actual).isEqualTo(codigo);
    }

    @Then("the listed bookings must contain id {int}")
    public void lasReservasListadasDebenContener(int id) {
        List<Integer> ids = JsonPath.read(respuesta.getResponseBody(), "$[*].id");
        assertThat(ids).contains(id);
    }
}
