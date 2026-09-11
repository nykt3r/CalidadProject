package steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import app.calidad.reservas.ReservasApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = ReservasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class usuarioSteps {

    @LocalServerPort
    private int port;

    private EntityExchangeResult<String> respuesta;

    @Given("the API has registered users")
    public void laApiCuentaConUsuariosRegistrados() {
    }

    @When("a client requests the list of users")
    public void unClienteConsultaElListadoDeUsuarios() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        respuesta = client.get().uri("/usuarios")
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
}