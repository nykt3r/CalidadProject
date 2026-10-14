// F0.3: el cliente propaga íntegro el ApiError del backend (caso 409 RESERVA_SOLAPADA).
import { describe, expect, it, vi } from 'vitest';
import { ApiError, request } from './client';

function respuestaMock(cuerpo: unknown, estado: number): Response {
  return {
    ok: estado >= 200 && estado < 300,
    status: estado,
    text: async () => JSON.stringify(cuerpo),
  } as Response;
}

describe('cliente API', () => {
  it('propaga íntegro un 409 RESERVA_SOLAPADA', async () => {
    const cuerpo = { status: 409, error: 'RESERVA_SOLAPADA', message: 'Horario ocupado' };
    const fetchFn = vi.fn(async () => respuestaMock(cuerpo, 409));
    const promesa = request<unknown>('http://localhost:8080', 'reservas', {
      metodo: 'POST',
      body: {},
      fetchFn: fetchFn as typeof fetch,
    });
    await expect(promesa).rejects.toMatchObject(cuerpo);
    try {
      await promesa;
      expect.unreachable('debió lanzar ApiError');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).toBody()).toEqual(cuerpo);
    }
  });

  it('devuelve los datos en respuestas 2xx', async () => {
    const fetchFn = vi.fn(async () => respuestaMock([{ id: 1 }], 200));
    const datos = await request<unknown[]>('http://localhost:8080', 'usuarios', {
      fetchFn: fetchFn as typeof fetch,
    });
    expect(datos).toEqual([{ id: 1 }]);
  });

  it('convierte fallos de red en FALLO_DE_RED', async () => {
    const fetchFn = vi.fn(async () => {
      throw new TypeError('network down');
    });
    await expect(
      request<unknown>('http://localhost:8080', 'usuarios', {
        fetchFn: fetchFn as typeof fetch,
      }),
    ).rejects.toMatchObject({ status: 0, error: 'FALLO_DE_RED' });
  });

  // F4.1: cada error del catálogo (§2.3) se propaga íntegro.
  it.each([
    { status: 400, error: 'VALIDACION' },
    { status: 404, error: 'NO_ENCONTRADO' },
    { status: 409, error: 'DUPLICADO' },
    { status: 409, error: 'USUARIO_INACTIVO' },
    { status: 409, error: 'RECURSO_EN_MANTENIMIENTO' },
    { status: 409, error: 'RECURSO_FUERA_DE_SERVICIO' },
    { status: 409, error: 'RESERVA_SOLAPADA' },
    { status: 409, error: 'RESERVA_FINALIZADA' },
    { status: 409, error: 'LIMITE_RESERVAS_ACTIVAS' },
  ])('propaga $error ($status) íntegro', async ({ status, error }) => {
    const cuerpo = { status, error, message: `mensaje de ${error}` };
    const fetchFn = vi.fn(async () => respuestaMock(cuerpo, status));
    const promesa = request<unknown>('http://localhost:8080', 'reservas', {
      metodo: 'POST',
      body: {},
      fetchFn: fetchFn as typeof fetch,
    });
    await expect(promesa).rejects.toBeInstanceOf(ApiError);
    await expect(promesa).rejects.toMatchObject(cuerpo);
  });

  it('responde ERROR_DESCONOCIDO si el cuerpo no tiene forma ApiError', async () => {
    const fetchFn = vi.fn(async () => respuestaMock({ raro: true }, 500));
    await expect(
      request<unknown>('http://localhost:8080', 'usuarios', {
        fetchFn: fetchFn as typeof fetch,
      }),
    ).rejects.toMatchObject({ status: 500, error: 'ERROR_DESCONOCIDO' });
  });
});
