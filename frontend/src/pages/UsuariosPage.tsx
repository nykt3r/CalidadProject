// Vista Usuarios: tabla (GET) con filtro por estado en cliente + formulario crear (POST).
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../api/api';
import type { EstadoUsuario, Usuario } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';
import UsuarioForm from '../components/UsuarioForm';

type Filtro = 'TODOS' | EstadoUsuario;

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [filtro, setFiltro] = useState<Filtro>('TODOS');
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .listarUsuarios()
      .then(setUsuarios)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, []);

  const visibles = usuarios.filter((u) => filtro === 'TODOS' || u.estado === filtro);

  return (
    <section>
      <h1>Usuarios</h1>
      <div className="toolbar">
        <label>
          Estado{' '}
          <select value={filtro} onChange={(e) => setFiltro(e.target.value as Filtro)}>
            <option value="TODOS">Todos</option>
            <option value="ACTIVO">ACTIVO</option>
            <option value="INACTIVO">INACTIVO</option>
          </select>
        </label>
      </div>
      <ErrorBanner error={error} onReintentar={cargar} />
      {cargando ? (
        <p className="estado-carga">Cargando…</p>
      ) : visibles.length === 0 ? (
        <p className="estado-vacio">Sin usuarios.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Documento</th>
              <th>Correo</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {visibles.map((u) => (
              <tr key={u.id}>
                <td>
                  <Link to={`/usuarios/${u.id}`}>{u.id}</Link>
                </td>
                <td>
                  <Link to={`/usuarios/${u.id}`}>{u.nombre}</Link>
                </td>
                <td>{u.documento}</td>
                <td>{u.correo}</td>
                <td>{u.estado}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <UsuarioForm onCreado={(creado) => setUsuarios((actual) => [...actual, creado])} />
    </section>
  );
}
