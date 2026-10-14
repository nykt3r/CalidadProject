// Vista disponibilidad: selector de recurso + rango de fechas → muestra
// `disponible` y la lista de reservas solapadas (GET disponibilidad).
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import { api } from '../api/api';
import type { Disponibilidad, Recurso } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

export default function DisponibilidadPage() {
  const [searchParams] = useSearchParams();
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [recursoId, setRecursoId] = useState(searchParams.get('recursoId') ?? '1');
  const [desde, setDesde] = useState('2026-10-20T09:00');
  const [hasta, setHasta] = useState('2026-10-20T11:00');
  const [resultado, setResultado] = useState<Disponibilidad | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    api.listarRecursos().then(setRecursos).catch(() => setRecursos([]));
  }, []);

  const consultar = async (e: React.FormEvent) => {
    e.preventDefault();
    setCargando(true);
    setError(null);
    try {
      const r = await api.consultarDisponibilidad(Number(recursoId), desde, hasta);
      setResultado(r);
    } catch (err) {
      setResultado(null);
      setError(err);
    } finally {
      setCargando(false);
    }
  };

  return (
    <section>
      <h1>Disponibilidad</h1>
      <form className="form" onSubmit={consultar}>
        <label>
          Recurso{' '}
          <select value={recursoId} onChange={(e) => setRecursoId(e.target.value)}>
            {recursos.map((r) => (
              <option key={r.id} value={r.id}>
                {r.nombre} ({r.estado})
              </option>
            ))}
          </select>
        </label>
        <label>
          Desde{' '}
          <input
            type="datetime-local"
            value={desde}
            onChange={(e) => setDesde(e.target.value)}
          />
        </label>
        <label>
          Hasta{' '}
          <input type="datetime-local" value={hasta} onChange={(e) => setHasta(e.target.value)} />
        </label>
        <button type="submit" disabled={cargando}>
          {cargando ? 'Consultando…' : 'Consultar'}
        </button>
      </form>
      <ErrorBanner error={error} />
      {resultado && (
        <>
          <p>
            {resultado.disponible ? 'Disponible' : 'No disponible'} (
            {resultado.reservasSolapadas.length} solapadas)
          </p>
          {resultado.reservasSolapadas.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Inicio</th>
                  <th>Fin</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {resultado.reservasSolapadas.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.usuario.nombre}</td>
                    <td>{r.fechaInicio}</td>
                    <td>{r.fechaFin}</td>
                    <td>{r.estado}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </section>
  );
}
