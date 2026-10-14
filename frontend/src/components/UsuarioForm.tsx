import { useState } from 'react';
import { api } from '../api/api';
import { esApiError } from '../api/client';
import type { Usuario, UsuarioRequest } from '../api/types';
import { sinErrores, validarUsuario } from '../utils/validacion';

const VACIO: UsuarioRequest = { nombre: '', documento: '', correo: '' };

export default function UsuarioForm({ onCreado }: { onCreado: (u: Usuario) => void }) {
  const [valores, setValores] = useState<UsuarioRequest>(VACIO);
  const [errores, setErrores] = useState<{ nombre?: string; documento?: string; correo?: string }>({});
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const cambiar =
    (campo: keyof UsuarioRequest) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setValores((v) => ({ ...v, [campo]: e.target.value }));
    };

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    const validacion = validarUsuario(valores);
    setErrores(validacion);
    setErrorGeneral(null);
    if (!sinErrores(validacion)) return;
    setEnviando(true);
    try {
      const creado = await api.crearUsuario(valores);
      setValores(VACIO);
      setErrores({});
      onCreado(creado);
    } catch (err) {
      // DUPLICADO (409): aviso junto al campo documento/correo, sin perder datos.
      if (esApiError(err) && err.error === 'DUPLICADO') {
        setErrores({ documento: err.message, correo: err.message });
      } else if (esApiError(err)) {
        setErrorGeneral(`${err.error}: ${err.message}`);
      } else {
        setErrorGeneral('Error inesperado al crear el usuario');
      }
    } finally {
      setEnviando(false);
    }
  };

  return (
    <form className="form" onSubmit={enviar}>
      <h2>Crear usuario</h2>
      <label>
        Nombre
        <input value={valores.nombre} onChange={cambiar('nombre')} />
        {errores.nombre && <span className="error-campo">{errores.nombre}</span>}
      </label>
      <label>
        Documento
        <input value={valores.documento} onChange={cambiar('documento')} />
        {errores.documento && <span className="error-campo">{errores.documento}</span>}
      </label>
      <label>
        Correo
        <input value={valores.correo} onChange={cambiar('correo')} />
        {errores.correo && <span className="error-campo">{errores.correo}</span>}
      </label>
      {errorGeneral && <div className="banner-error">{errorGeneral}</div>}
      <button type="submit" disabled={enviando}>
        {enviando ? 'Creando…' : 'Crear'}
      </button>
    </form>
  );
}
