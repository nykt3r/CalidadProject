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
});
