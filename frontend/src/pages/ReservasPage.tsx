// Vista Reservas: agenda del día (?fecha=), por usuario (?usuarioId=&estado=)
// y por recurso (?recursoId=&desde=&hasta=).
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { api } from '../api/api';
import type { EstadoReserva, Recurso, Reserva, Usuario } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

type Vista = 'dia' | 'usuario' | 'recurso';

function hoy(): string {
  return new Date().toISOString().slice(0, 10);
}

function TablaReservas({ reservas }: { reservas: Reserva[] }) {
  if (reservas.length === 0) return <p className="estado-vacio">Sin reservas.</p>;
  return (
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Usuario</th>
          <th>Recurso</th>
          <th>Inicio</th>
          <th>Fin</th>
          <th>Estado</th>
        </tr>
      </thead>
      <tbody>
        {reservas.map((r) => (
          <tr key={r.id}>
            <td>
              <Link to={`/reservas/${r.id}`}>{r.id}</Link>
            </td>
            <td>{r.usuario.nombre}</td>
            <td>{r.recurso.nombre}</td>
            <td>{r.fechaInicio}</td>
            <td>{r.fechaFin}</td>
            <td>{r.estado}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default function ReservasPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const vista = (searchParams.get('vista') ?? 'dia') as Vista;
  const fecha = searchParams.get('fecha') ?? hoy();
  const usuarioId = searchParams.get('usuarioId') ?? '';
  const recursoId = searchParams.get('recursoId') ?? '';
  const estado = searchParams.get('estado') ?? '';
  const desde = searchParams.get('desde') ?? '';
  const hasta = searchParams.get('hasta') ?? '';

  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [reservas, setReservas] = useState<Reserva[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    api.listarUsuarios().then(setUsuarios).catch(() => setUsuarios([]));
    api.listarRecursos().then(setRecursos).catch(() => setRecursos([]));
  }, []);

  useEffect(() => {
    setCargando(true);
    setError(null);
    const consulta: Promise<Reserva[]> =
      vista === 'usuario' && usuarioId
        ? api.reservasPorUsuario(Number(usuarioId), (estado || undefined) as EstadoReserva | undefined)
        : vista === 'recurso' && recursoId
          ? api.reservasPorRecurso(Number(recursoId), {
              desde: desde || undefined,
              hasta: hasta || undefined,
            })
          : api.reservasPorFecha(fecha);
    consulta.then(setReservas).catch(setError).finally(() => setCargando(false));
  }, [vista, fecha, usuarioId, recursoId, estado, desde, hasta, intento]);

  const fijar = (clave: string, valor: string) => {
    const siguientes = new URLSearchParams(searchParams);
    if (valor) siguientes.set(clave, valor);
    else siguientes.delete(clave);
    setSearchParams(siguientes);
  };

  const cambiarVista = (v: Vista) => {
    const siguientes = new URLSearchParams();
    siguientes.set('vista', v);
    if (v === 'dia') siguientes.set('fecha', fecha);
    setSearchParams(siguientes);
  };

  return (
    <section>
      <h1>Reservas</h1>
      <div className="toolbar">
        {(['dia', 'usuario', 'recurso'] as Vista[]).map((v) => (
          <button
            key={v}
            type="button"
            disabled={vista === v}
            onClick={() => cambiarVista(v)}
          >
            {v === 'dia' ? 'Por día' : v === 'usuario' ? 'Por usuario' : 'Por recurso'}
          </button>
        ))}
        <Link to="/reservas/nueva">Crear reserva</Link>
      </div>

      {vista === 'dia' && (
        <div className="toolbar">
          <label>
            Fecha{' '}
            <input type="date" value={fecha} onChange={(e) => fijar('fecha', e.target.value)} />
          </label>
        </div>
      )}
      {vista === 'usuario' && (
        <div className="toolbar">
          <label>
            Usuario{' '}
            <select value={usuarioId} onChange={(e) => fijar('usuarioId', e.target.value)}>
              <option value="">Elegir…</option>
              {usuarios.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.nombre} ({u.estado})
                </option>
              ))}
            </select>
          </label>
          <label>
            Estado{' '}
            <select value={estado} onChange={(e) => fijar('estado', e.target.value)}>
              <option value="">Todos</option>
              <option value="ACTIVA">ACTIVA</option>
              <option value="CANCELADA">CANCELADA</option>
              <option value="FINALIZADA">FINALIZADA</option>
            </select>
          </label>
        </div>
      )}
      {vista === 'recurso' && (
        <div className="toolbar">
          <label>
            Recurso{' '}
            <select value={recursoId} onChange={(e) => fijar('recursoId', e.target.value)}>
              <option value="">Elegir…</option>
              {recursos.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.nombre}
                </option>
              ))}
            </select>
          </label>
          <label>
            Desde{' '}
            <input
              type="datetime-local"
              value={desde}
              onChange={(e) => fijar('desde', e.target.value)}
            />
          </label>
          <label>
            Hasta{' '}
            <input
              type="datetime-local"
              value={hasta}
              onChange={(e) => fijar('hasta', e.target.value)}
            />
          </label>
        </div>
      )}

      <ErrorBanner
        error={error}
        onReintentar={() => setIntento((i) => i + 1)}
      />
      {cargando ? <p className="estado-carga">Cargando…</p> : <TablaReservas reservas={reservas} />}
    </section>
  );
}
