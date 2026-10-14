// Vista Disponibilidad (F0: consulta recurso + rango contra mocks).
import { useState } from 'react';
import { api } from '../api/api';
import type { Disponibilidad } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

export default function DisponibilidadPage() {
  const [recursoId, setRecursoId] = useState('1');
  const [desde, setDesde] = useState('2026-10-20T09:00');
  const [hasta, setHasta] = useState('2026-10-20T11:00');
  const [resultado, setResultado] = useState<Disponibilidad | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<unknown>(null);

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
          Recurso ID <input value={recursoId} onChange={(e) => setRecursoId(e.target.value)} />
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
        <p>
          {resultado.disponible ? 'Disponible' : 'No disponible'} (
          {resultado.reservasSolapadas.length} solapadas)
        </p>
      )}
    </section>
  );
}
