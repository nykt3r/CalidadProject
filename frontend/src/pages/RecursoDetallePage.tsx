// Detalle de recurso (GET {id}) + cambio de estado (PATCH) con selector y confirmación.
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { EstadoRecurso, Recurso } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

const ESTADOS: EstadoRecurso[] = ['DISPONIBLE', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO'];

export default function RecursoDetallePage() {
  const { id } = useParams();
  const [recurso, setRecurso] = useState<Recurso | null>(null);
  const [nuevoEstado, setNuevoEstado] = useState<EstadoRecurso>('DISPONIBLE');
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [cambiando, setCambiando] = useState(false);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .obtenerRecurso(Number(id))
      .then((r) => {
        setRecurso(r);
        setNuevoEstado(r.estado);
      })
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, [id]);

  const cambiarEstado = async () => {
    if (!recurso || nuevoEstado === recurso.estado) return;
    if (!window.confirm(`¿Cambiar ${recurso.nombre} a ${nuevoEstado}?`)) return;
    setCambiando(true);
    try {
      const actualizado = await api.cambiarEstadoRecurso(recurso.id, nuevoEstado);
      setRecurso(actualizado);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setCambiando(false);
    }
  };

  if (cargando) return <p className="estado-carga">Cargando…</p>;

  if (esApiError(error) && error.error === 'NO_ENCONTRADO') {
    return (
      <section>
        <h1>Recurso no encontrado</h1>
        <p>No existe el recurso con id {id}.</p>
        <p>
          <Link to="/recursos">Volver a recursos</Link>
        </p>
      </section>
    );
  }

  if (error || !recurso) return <ErrorBanner error={error ?? 'Recurso no disponible'} onReintentar={cargar} />;

  return (
    <section>
      <h1>{recurso.nombre}</h1>
      <dl>
        <dt>ID</dt>
        <dd>{recurso.id}</dd>
        <dt>Descripción</dt>
        <dd>{recurso.descripcion}</dd>
        <dt>Tipo</dt>
        <dd>{recurso.tipo}</dd>
        <dt>Capacidad</dt>
        <dd>{recurso.capacidad}</dd>
        <dt>Estado</dt>
        <dd>{recurso.estado}</dd>
      </dl>
      <div className="toolbar">
        <label>
          Nuevo estado{' '}
          <select
            value={nuevoEstado}
            onChange={(e) => setNuevoEstado(e.target.value as EstadoRecurso)}
          >
            {ESTADOS.map((e) => (
              <option key={e} value={e}>
                {e}
              </option>
            ))}
          </select>
        </label>
        <button
          type="button"
          onClick={cambiarEstado}
          disabled={cambiando || nuevoEstado === recurso.estado}
        >
          {cambiando ? 'Cambiando…' : 'Cambiar estado'}
        </button>
        <Link to={`/recursos/${recurso.id}/editar`}>Editar</Link>
        <Link to={`/disponibilidad?recursoId=${recurso.id}`}>Ver disponibilidad</Link>
        <Link to="/recursos">Volver</Link>
      </div>
      <ErrorBanner error={null} />
    </section>
  );
}
