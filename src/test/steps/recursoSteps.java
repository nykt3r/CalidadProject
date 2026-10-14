package steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import app.calidad.reservas.repository.RecursoRepository;

public class recursoSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private RecursoRepository recursoRepository;

    private EntityExchangeResult<String> respuesta;

    private Long recursoCreadoId;

    private RestTestClient client() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Before
    @After
    public void limpiarRecursosDePrueba() {
        recursoRepository.findAll().stream()
                .filter(r -> r.getNombre() != null && r.getNombre().startsWith("TEST-"))
                .forEach(recursoRepository::delete);
        recursoCreadoId = null;
    }

    @When("a client creates the resource {string} of type {string} with capacity {int}")
    public void unClienteCreaElRecurso(String nombre, String tipo, int capacidad) {
        String body = "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Recurso de prueba\","
                + "\"tipo\":\"" + tipo + "\",\"capacidad\":" + capacidad + "}";
        respuesta = client().post().uri("/recursos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
        if (respuesta.getStatus() == HttpStatus.CREATED) {
            Number id = JsonPath.read(respuesta.getResponseBody(), "$.id");
            recursoCreadoId = id.longValue();
        }
    }

    @When("a client creates a resource without capacity")
    public void unClienteCreaUnRecursoSinCapacidad() {
        String body = "{\"nombre\":\"TEST-SinCapacidad\",\"tipo\":\"LABORATORIO\"}";
        respuesta = client().post().uri("/recursos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client requests the created resource by id")
    public void unClienteConsultaElRecursoCreado() {
        respuesta = client().get().uri("/recursos/" + recursoCreadoId)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client requests the resource with id {int}")
    public void unClienteConsultaElRecursoPorId(int id) {
        respuesta = client().get().uri("/recursos/" + id)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client lists resources with state {string}")
    public void unClienteListaRecursosPorEstado(String estado) {
        respuesta = client().get().uri("/recursos?estado=" + estado)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client fully updates the created resource to {string} with capacity {int}")
    public void unClienteActualizaElRecursoCreado(String nombre, int capacidad) {
        String body = "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Actualizado\","
                + "\"tipo\":\"AUDITORIO\",\"capacidad\":" + capacidad + "}";
        respuesta = client().put().uri("/recursos/" + recursoCreadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client fully updates the resource with id {int}")
    public void unClienteActualizaElRecursoPorId(int id) {
        String body = "{\"nombre\":\"TEST-X\",\"tipo\":\"OTRO\",\"capacidad\":1}";
        respuesta = client().put().uri("/recursos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client changes the created resource state to {string}")
    public void unClienteCambiaElEstadoDelRecursoCreado(String estado) {
        String body = "{\"estado\":\"" + estado + "\"}";
        respuesta = client().patch().uri("/recursos/" + recursoCreadoId + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client checks availability of resource {int} from {string} to {string}")
    public void unClienteConsultaDisponibilidad(int id, String desde, String hasta) {
        respuesta = client().get().uri("/recursos/" + id + "/disponibilidad?desde=" + desde + "&hasta=" + hasta)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @Then("the resource response status must be {int}")
    public void elEstadoDeLaRespuestaDelRecursoDebeSer(int estado) {
        assertThat(respuesta.getStatus().value()).isEqualTo(estado);
    }

    @Then("the resource field {string} must be {string}")
    public void elCampoDelRecursoDebeSer(String campo, String valor) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$." + campo);
        assertThat(actual).isEqualTo(valor);
    }

    @Then("the listed resources must contain {string}")
    public void losRecursosListadosDebenContener(String nombre) {
        List<String> nombres = JsonPath.read(respuesta.getResponseBody(), "$[*].nombre");
        assertThat(nombres).contains(nombre);
    }

    @Then("the resource error code must be {string}")
    public void elCodigoDeErrorDelRecursoDebeSer(String codigo) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$.error");
        assertThat(actual).isEqualTo(codigo);
    }

    @Then("the availability must be {string}")
    public void laDisponibilidadDebeSer(String valor) {
        Boolean actual = JsonPath.read(respuesta.getResponseBody(), "$.disponible");
        assertThat(actual).isEqualTo(Boolean.parseBoolean(valor));
    }

    @Then("the overlapping list must contain {int} reservations")
    public void laListaDeSolapadasDebeContener(int cantidad) {
        List<Object> solapadas = JsonPath.read(respuesta.getResponseBody(), "$.reservasSolapadas");
        assertThat(solapadas).hasSize(cantidad);
    }
}
