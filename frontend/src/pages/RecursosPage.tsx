// Vista Recursos: tabla con filtros por estado y tipo vía query params reales
// (?estado=, ?tipo=) reflejados en la URL.
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { api } from '../api/api';
import type { EstadoRecurso, Recurso, TipoRecurso } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

export default function RecursosPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const estado = searchParams.get('estado') ?? '';
  const tipo = searchParams.get('tipo') ?? '';
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    setCargando(true);
    setError(null);
    api
      .listarRecursos({
        estado: (estado || undefined) as EstadoRecurso | undefined,
        tipo: (tipo || undefined) as TipoRecurso | undefined,
      })
      .then(setRecursos)
      .catch(setError)
      .finally(() => setCargando(false));
  }, [estado, tipo]);

  const fijar = (clave: string, valor: string) => {
    const siguientes = new URLSearchParams(searchParams);
    if (valor) siguientes.set(clave, valor);
    else siguientes.delete(clave);
    setSearchParams(siguientes);
  };

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .listarRecursos({
        estado: (estado || undefined) as EstadoRecurso | undefined,
        tipo: (tipo || undefined) as TipoRecurso | undefined,
      })
      .then(setRecursos)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  return (
    <section>
      <h1>Recursos</h1>
      <div className="toolbar">
        <label>
          Estado{' '}
          <select value={estado} onChange={(e) => fijar('estado', e.target.value)}>
            <option value="">Todos</option>
            <option value="DISPONIBLE">DISPONIBLE</option>
            <option value="MANTENIMIENTO">MANTENIMIENTO</option>
            <option value="FUERA_DE_SERVICIO">FUERA_DE_SERVICIO</option>
          </select>
        </label>
        <label>
          Tipo{' '}
          <select value={tipo} onChange={(e) => fijar('tipo', e.target.value)}>
            <option value="">Todos</option>
            <option value="LABORATORIO">LABORATORIO</option>
            <option value="AUDITORIO">AUDITORIO</option>
            <option value="SALA_REUNIONES">SALA_REUNIONES</option>
            <option value="EQUIPO_AUDIOVISUAL">EQUIPO_AUDIOVISUAL</option>
            <option value="OTRO">OTRO</option>
          </select>
        </label>
        <Link to="/recursos/nuevo">Crear recurso</Link>
      </div>
      <ErrorBanner error={error} onReintentar={cargar} />
      {cargando ? (
        <p className="estado-carga">Cargando…</p>
      ) : recursos.length === 0 ? (
        <p className="estado-vacio">Sin recursos.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Tipo</th>
              <th>Capacidad</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {recursos.map((r) => (
              <tr key={r.id}>
                <td>
                  <Link to={`/recursos/${r.id}`}>{r.id}</Link>
                </td>
                <td>
                  <Link to={`/recursos/${r.id}`}>{r.nombre}</Link>
                </td>
                <td>{r.tipo}</td>
                <td>{r.capacidad}</td>
                <td>{r.estado}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
