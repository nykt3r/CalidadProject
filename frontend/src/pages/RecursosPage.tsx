// Vista Recursos (F0: tabla contra mocks; F2 añade CRUD/filtros).
import { useEffect, useState } from 'react';
import { api } from '../api/api';
import type { Recurso } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';

export default function RecursosPage() {
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .listarRecursos()
      .then(setRecursos)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, []);

  return (
    <section>
      <h1>Recursos</h1>
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
                <td>{r.id}</td>
                <td>{r.nombre}</td>
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
