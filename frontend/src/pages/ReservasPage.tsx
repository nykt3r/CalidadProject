// Vista Reservas (F0: agenda del día contra mocks; F3 añade crear/cancelar).
import { useEffect, useState } from 'react';
import { api } from '../api/api';
import type { Reserva } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

function hoy(): string {
  return new Date().toISOString().slice(0, 10);
}

export default function ReservasPage() {
  const [fecha, setFecha] = useState(hoy());
  const [reservas, setReservas] = useState<Reserva[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .reservasPorFecha(fecha)
      .then(setReservas)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, [fecha]);

  return (
    <section>
      <h1>Reservas</h1>
      <div className="toolbar">
        <label>
          Fecha{' '}
          <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
        </label>
      </div>
      <ErrorBanner error={error} onReintentar={cargar} />
      {cargando ? (
        <p className="estado-carga">Cargando…</p>
      ) : reservas.length === 0 ? (
        <p className="estado-vacio">Sin reservas para {fecha}.</p>
      ) : (
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
                <td>{r.id}</td>
                <td>{r.usuario.nombre}</td>
                <td>{r.recurso.nombre}</td>
                <td>{r.fechaInicio}</td>
                <td>{r.fechaFin}</td>
                <td>{r.estado}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
