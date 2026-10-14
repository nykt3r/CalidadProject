// Formulario crear reserva: selectores de usuario ACTIVO y recurso DISPONIBLE,
// validación cliente (reglas 4 y 7), pre-chequeo de disponibilidad antes de enviar
// y aviso anticipado de "máx. 3 activas". El 409 del backend es el árbitro final.
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { Recurso, Usuario } from '../api/types';
import { sugerenciaPara } from '../utils/errores';
import { sinErroresReserva, validarReserva } from '../utils/validacion';

export default function ReservaForm() {
  const navigate = useNavigate();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [usuarioId, setUsuarioId] = useState('');
  const [recursoId, setRecursoId] = useState('');
  const [inicio, setInicio] = useState('');
  const [fin, setFin] = useState('');
  const [errorFechas, setErrorFechas] = useState<string | undefined>(undefined);
  const [aviso, setAviso] = useState<string | null>(null);
  const [errorNegocio, setErrorNegocio] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    api.listarUsuarios().then((u) => {
      const activos = u.filter((x) => x.estado === 'ACTIVO');
      setUsuarios(activos);
      if (activos.length > 0) setUsuarioId(String(activos[0].id));
    });
    api.listarRecursos({ estado: 'DISPONIBLE' }).then((r) => {
      setRecursos(r);
      if (r.length > 0) setRecursoId(String(r[0].id));
    });
  }, []);

  // F3.4: anticipa el aviso de "máx. 3 activas" al elegir usuario.
  useEffect(() => {
    if (!usuarioId) {
      setAviso(null);
      return;
    }
    api
      .reservasPorUsuario(Number(usuarioId), 'ACTIVA')
      .then((activas) => {
        setAviso(
          activas.length >= 3
            ? `Este usuario ya tiene ${activas.length} reservas activas (máx. 3): el backend rechazará la creación.`
            : null,
        );
      })
      .catch(() => setAviso(null));
  }, [usuarioId]);

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    const validacion = validarReserva({
      usuarioId: Number(usuarioId),
      recursoId: Number(recursoId),
      fechaInicio: inicio,
      fechaFin: fin,
    });
    setErrorFechas(validacion.fechas);
    setErrorNegocio(null);
    if (!sinErroresReserva(validacion)) return;
    setEnviando(true);
    try {
      // Pre-chequeo de disponibilidad antes de enviar.
      const disp = await api.consultarDisponibilidad(Number(recursoId), inicio, fin);
      if (!disp.disponible) {
        const nombres = disp.reservasSolapadas.map((r) => `#${r.id}`).join(', ');
        setErrorNegocio(
          `No disponible en ese horario${nombres ? ` (solapa con ${nombres})` : ''}. ${sugerenciaPara('RESERVA_SOLAPADA') ?? ''}`,
        );
        return;
      }
      const creada = await api.crearReserva({
        usuarioId: Number(usuarioId),
        recursoId: Number(recursoId),
        fechaInicio: inicio,
        fechaFin: fin,
      });
      navigate(`/reservas/${creada.id}`);
    } catch (err) {
      // Al 409 muestra el message del backend + sugerencia, sin perder los datos.
      if (esApiError(err)) {
        const sugerencia = sugerenciaPara(err.error);
        setErrorNegocio(`${err.error}: ${err.message}${sugerencia ? ` ${sugerencia}` : ''}`);
      } else {
        setErrorNegocio('Error inesperado al crear la reserva');
      }
    } finally {
      setEnviando(false);
    }
  };

  return (
    <form className="form" onSubmit={enviar}>
      <label>
        Usuario (ACTIVO)
        <select value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)}>
          {usuarios.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nombre}
            </option>
          ))}
        </select>
      </label>
      {aviso && <div className="banner-aviso">{aviso}</div>}
      <label>
        Recurso (DISPONIBLE)
        <select value={recursoId} onChange={(e) => setRecursoId(e.target.value)}>
          {recursos.map((r) => (
            <option key={r.id} value={r.id}>
              {r.nombre}
            </option>
          ))}
        </select>
      </label>
      <label>
        Inicio
        <input type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} />
      </label>
      <label>
        Fin
        <input type="datetime-local" value={fin} onChange={(e) => setFin(e.target.value)} />
      </label>
      {errorFechas && <span className="error-campo">{errorFechas}</span>}
      {errorNegocio && <div className="banner-error">{errorNegocio}</div>}
      <button type="submit" disabled={enviando}>
        {enviando ? 'Reservando…' : 'Reservar'}
      </button>
    </form>
  );
}
