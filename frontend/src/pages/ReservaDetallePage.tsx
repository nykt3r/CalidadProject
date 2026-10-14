// Detalle de reserva (GET {id}) + cancelar (DELETE) con confirmación.
// Tras cancelar muestra CANCELADA; el caso RESERVA_FINALIZADA muestra aviso.
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { Reserva } from '../api/types';
import ErrorBanner from '../components/ErrorBanner';
import { sugerenciaPara } from '../utils/errores';

export default function ReservaDetallePage() {
  const { id } = useParams();
  const [reserva, setReserva] = useState<Reserva | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [cancelando, setCancelando] = useState(false);

  const cargar = () => {
    setCargando(true);
    setError(null);
    api
      .obtenerReserva(Number(id))
      .then(setReserva)
      .catch(setError)
      .finally(() => setCargando(false));
  };

  useEffect(cargar, [id]);

  const cancelar = async () => {
    if (!reserva) return;
    if (!window.confirm(`¿Cancelar la reserva #${reserva.id}?`)) return;
    setCancelando(true);
    setAviso(null);
    try {
      const actualizada = await api.cancelarReserva(reserva.id);
      setReserva(actualizada);
      setError(null);
    } catch (err) {
      if (esApiError(err) && err.error === 'RESERVA_FINALIZADA') {
        setAviso(`${err.error}: ${err.message} ${sugerenciaPara(err.error) ?? ''}`);
      } else {
        setError(err);
      }
    } finally {
      setCancelando(false);
    }
  };

  if (cargando) return <p className="estado-carga">Cargando…</p>;

  if (esApiError(error) && error.error === 'NO_ENCONTRADO') {
    return (
      <section>
        <h1>Reserva no encontrada</h1>
        <p>No existe la reserva con id {id}.</p>
        <p>
          <Link to="/reservas">Volver a reservas</Link>
        </p>
      </section>
    );
  }

  if (error || !reserva) return <ErrorBanner error={error ?? 'Reserva no disponible'} onReintentar={cargar} />;

  return (
    <section>
      <h1>Reserva #{reserva.id}</h1>
      <dl>
        <dt>Usuario</dt>
        <dd>
          {reserva.usuario.nombre} ({reserva.usuario.estado})
        </dd>
        <dt>Recurso</dt>
        <dd>
          {reserva.recurso.nombre} ({reserva.recurso.estado})
        </dd>
        <dt>Inicio</dt>
        <dd>{reserva.fechaInicio}</dd>
        <dt>Fin</dt>
        <dd>{reserva.fechaFin}</dd>
        <dt>Estado</dt>
        <dd>{reserva.estado}</dd>
        <dt>Creada</dt>
        <dd>{reserva.fechaCreacion}</dd>
      </dl>
      {aviso && <div className="banner-aviso">{aviso}</div>}
      <div className="toolbar">
        {reserva.estado === 'ACTIVA' && (
          <button type="button" onClick={cancelar} disabled={cancelando}>
            {cancelando ? 'Cancelando…' : 'Cancelar reserva'}
          </button>
        )}
        <Link to="/reservas">Volver</Link>
      </div>
    </section>
  );
}
