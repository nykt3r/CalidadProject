// Punto de acceso único a la API para las vistas.
// Por defecto usa mocks (VITE_USE_MOCKS=true) para desarrollo independiente;
// con VITE_USE_MOCKS=false apunta al backend real (acople F5).

import { baseUrlPorDefecto, createHttpApi } from '../api/client';
import type { ReservasApi } from '../api/types';
import { createMockApi } from '../mocks/mockClient';

function usaMocks(): boolean {
  const flag = import.meta.env.VITE_USE_MOCKS as string | undefined;
  return flag !== 'false';
}

export const api: ReservasApi = usaMocks() ? createMockApi() : createHttpApi(baseUrlPorDefecto());

export const modoApi = usaMocks() ? 'mock' : 'real';
