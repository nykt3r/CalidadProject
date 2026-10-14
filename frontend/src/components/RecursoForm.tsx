// Formulario compartido crear (POST) / editar (PUT) de recursos.
import { useState } from 'react';
import { useNavigate } from 'react-router';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { Recurso, RecursoRequest, TipoRecurso } from '../api/types';
import { sinErroresRecurso, validarRecurso } from '../utils/validacion';

const TIPOS: TipoRecurso[] = ['LABORATORIO', 'AUDITORIO', 'SALA_REUNIONES', 'EQUIPO_AUDIOVISUAL', 'OTRO'];

export default function RecursoForm({ inicial }: { inicial?: Recurso }) {
  const esEdicion = inicial !== undefined;
  const navigate = useNavigate();
  const [valores, setValores] = useState<RecursoRequest>({
    nombre: inicial?.nombre ?? '',
    descripcion: inicial?.descripcion ?? '',
    tipo: inicial?.tipo ?? 'LABORATORIO',
    capacidad: inicial?.capacidad ?? 1,
  });
  const [errores, setErrores] = useState<{ nombre?: string; capacidad?: string }>({});
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    const validacion = validarRecurso(valores);
    setErrores(validacion);
    setErrorGeneral(null);
    if (!sinErroresRecurso(validacion)) return;
    setEnviando(true);
    try {
      if (esEdicion && inicial) {
        await api.modificarRecurso(inicial.id, valores);
        navigate(`/recursos/${inicial.id}`);
      } else {
        const creado = await api.crearRecurso(valores);
        navigate(`/recursos/${creado.id}`);
      }
    } catch (err) {
      if (esApiError(err)) setErrorGeneral(`${err.error}: ${err.message}`);
      else setErrorGeneral('Error inesperado al guardar el recurso');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <form className="form" onSubmit={enviar}>
      <label>
        Nombre
        <input
          value={valores.nombre}
          onChange={(e) => setValores((v) => ({ ...v, nombre: e.target.value }))}
        />
        {errores.nombre && <span className="error-campo">{errores.nombre}</span>}
      </label>
      <label>
        Descripción
        <input
          value={valores.descripcion}
          onChange={(e) => setValores((v) => ({ ...v, descripcion: e.target.value }))}
        />
      </label>
      <label>
        Tipo
        <select
          value={valores.tipo}
          onChange={(e) => setValores((v) => ({ ...v, tipo: e.target.value as TipoRecurso }))}
        >
          {TIPOS.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
      </label>
      <label>
        Capacidad
        <input
          type="number"
          min={1}
          value={valores.capacidad}
          onChange={(e) => setValores((v) => ({ ...v, capacidad: Number(e.target.value) }))}
        />
        {errores.capacidad && <span className="error-campo">{errores.capacidad}</span>}
      </label>
      {errorGeneral && <div className="banner-error">{errorGeneral}</div>}
      <button type="submit" disabled={enviando}>
        {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear'}
      </button>
    </form>
  );
}
