package app.calidad.reservas.repository;

import app.calidad.reservas.entity.EstadoUsuario;
import app.calidad.reservas.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByDocumento(String documento);

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findByEstado(EstadoUsuario estado);
}