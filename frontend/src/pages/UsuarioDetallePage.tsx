// Detalle de usuario (GET {id}) + activar/inactivar (PATCH) con confirmación.
// NO_ENCONTRADO (404) → vista dedicada con enlace de retorno.
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { Usuario } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

export default function UsuarioDetallePage() {
  const { id } = useParams();
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [cambiando, setCambiando] = useState(false);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .obtenerUsuario(Number(id))
      .then(setUsuario)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, [id]);

  const cambiarEstado = async () => {
    if (!usuario) return;
    const siguiente = usuario.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    const accion = siguiente === 'ACTIVO' ? 'activar' : 'inactivar';
    if (!window.confirm(`¿${accion} a ${usuario.nombre}?`)) return;
    setCambiando(true);
    try {
      const actualizado = await api.cambiarEstadoUsuario(usuario.id, siguiente);
      setUsuario(actualizado);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setCambiando(false);
    }
  };

  if (cargando) return <p className="estado-carga">Cargando…</p>;

  // Vista dedicada NO_ENCONTRADO con enlace de retorno.
  if (esApiError(error) && error.error === 'NO_ENCONTRADO') {
    return (
      <section>
        <h1>Usuario no encontrado</h1>
        <p>No existe el usuario con id {id}.</p>
        <p>
          <Link to="/usuarios">Volver a usuarios</Link>
        </p>
      </section>
    );
  }

  if (error || !usuario) return <ErrorBanner error={error ?? 'Usuario no disponible'} onReintentar={cargar} />;

  return (
    <section>
      <h1>{usuario.nombre}</h1>
      <ErrorBanner error={null} />
      <dl>
        <dt>ID</dt>
        <dd>{usuario.id}</dd>
        <dt>Documento</dt>
        <dd>{usuario.documento}</dd>
        <dt>Correo</dt>
        <dd>{usuario.correo}</dd>
        <dt>Estado</dt>
        <dd>{usuario.estado}</dd>
      </dl>
      <div className="toolbar">
        <button type="button" onClick={cambiarEstado} disabled={cambiando}>
          {cambiando
            ? 'Cambiando…'
            : usuario.estado === 'ACTIVO'
              ? 'Inactivar'
              : 'Activar'}
        </button>
        <Link to="/usuarios">Volver</Link>
      </div>
    </section>
  );
}
