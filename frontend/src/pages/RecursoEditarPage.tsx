// Editar recurso (PUT): carga el actual y reutiliza el formulario.
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { Recurso } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';
import RecursoForm from '../components/RecursoForm';

export default function RecursoEditarPage() {
  const { id } = useParams();
  const [recurso, setRecurso] = useState<Recurso | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .obtenerRecurso(Number(id))
      .then(setRecurso)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, [id]);

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
      <h1>Editar recurso {recurso.id}</h1>
      <RecursoForm inicial={recurso} />
    </section>
  );
}
