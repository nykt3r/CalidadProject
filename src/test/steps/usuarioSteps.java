package steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import app.calidad.reservas.ReservasApplication;
import app.calidad.reservas.repository.UsuarioRepository;

@CucumberContextConfiguration
@SpringBootTest(classes = ReservasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class usuarioSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private EntityExchangeResult<String> respuesta;

    private Long usuarioCreadoId;

    private RestTestClient client() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Before
    @After
    public void limpiarUsuariosDePrueba() {
        usuarioRepository.findAll().stream()
                .filter(u -> u.getDocumento() != null && u.getDocumento().startsWith("TEST-"))
                .forEach(usuarioRepository::delete);
        usuarioCreadoId = null;
    }

    @Given("the API has registered users")
    public void laApiCuentaConUsuariosRegistrados() {
    }

    @When("a client requests the list of users")
    public void unClienteConsultaElListadoDeUsuarios() {
        respuesta = client().get().uri("/usuarios")
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @Then("the response must be successful")
    public void laRespuestaDebeSerExitosa() {
        assertThat(respuesta.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @And("the list must contain {int} users")
    public void elListadoDebeContenerUsuarios(int cantidad) {
        List<Object> usuarios = JsonPath.read(respuesta.getResponseBody(), "$");
        assertThat(usuarios).hasSize(cantidad);
    }

    @And("the user {string} must be {string}")
    public void elUsuarioDebeEstarEnEstado(String nombre, String estado) {
        List<String> nombres = JsonPath.read(respuesta.getResponseBody(), "$[*].nombre");
        List<String> estados = JsonPath.read(respuesta.getResponseBody(), "$[*].estado");
        int indice = nombres.indexOf(nombre);
        assertThat(indice).as("usuario '%s'", nombre).isGreaterThanOrEqualTo(0);
        assertThat(estados.get(indice)).isEqualTo(estado);
    }

    @When("a client creates the user {string} with document {string} and email {string}")
    public void unClienteCreaElUsuario(String nombre, String documento, String correo) {
        String body = "{\"nombre\":\"" + nombre + "\",\"documento\":\"" + documento + "\",\"correo\":\"" + correo + "\"}";
        respuesta = client().post().uri("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
        if (respuesta.getStatus() == HttpStatus.CREATED) {
            Number id = JsonPath.read(respuesta.getResponseBody(), "$.id");
            usuarioCreadoId = id.longValue();
        }
    }

    @When("a client creates a user without name")
    public void unClienteCreaUnUsuarioSinNombre() {
        String body = "{\"nombre\":\"\",\"documento\":\"TEST-NONAME\",\"correo\":\"noname@example.com\"}";
        respuesta = client().post().uri("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client requests the created user by id")
    public void unClienteConsultaElUsuarioCreado() {
        respuesta = client().get().uri("/usuarios/" + usuarioCreadoId)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client requests the user with id {int}")
    public void unClienteConsultaElUsuarioPorId(int id) {
        respuesta = client().get().uri("/usuarios/" + id)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @When("a client changes the created user state to {string}")
    public void unClienteCambiaElEstadoDelUsuarioCreado(String estado) {
        String body = "{\"estado\":\"" + estado + "\"}";
        respuesta = client().patch().uri("/usuarios/" + usuarioCreadoId + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectBody(String.class)
                .returnResult();
    }

    @Then("the response status must be {int}")
    public void elEstadoDeLaRespuestaDebeSer(int estado) {
        assertThat(respuesta.getStatus().value()).isEqualTo(estado);
    }

    @And("the created user state must be {string}")
    public void elEstadoDelUsuarioCreadoDebeSer(String estado) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$.estado");
        assertThat(actual).isEqualTo(estado);
    }

    @And("the response field {string} must be {string}")
    public void elCampoDeLaRespuestaDebeSer(String campo, String valor) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$." + campo);
        assertThat(actual).isEqualTo(valor);
    }

    @And("the error code must be {string}")
    public void elCodigoDeErrorDebeSer(String codigo) {
        String actual = JsonPath.read(respuesta.getResponseBody(), "$.error");
        assertThat(actual).isEqualTo(codigo);
    }
}
