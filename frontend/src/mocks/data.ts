// Datos semilla del mock: espejo de data.sql + recursos/reservas demo coherentes.
// Permiten desarrollar la UI sin el backend (frontend-plan.md F0.2).

import type { Recurso, Reserva, Usuario } from '../api/types';

export const usuariosMock: Usuario[] = [
  { id: 1, nombre: 'Ana García', documento: 'DOC-001', correo: 'ana@example.com', estado: 'ACTIVO' },
  { id: 2, nombre: 'Luis Pérez', documento: 'DOC-002', correo: 'luis@example.com', estado: 'INACTIVO' },
];

export const recursosMock: Recurso[] = [
  {
    id: 1,
    nombre: 'Lab 101',
    descripcion: 'Laboratorio de cómputo',
    tipo: 'LABORATORIO',
    capacidad: 30,
    estado: 'DISPONIBLE',
  },
  {
    id: 2,
    nombre: 'Auditorio Central',
    descripcion: 'Auditorio principal',
    tipo: 'AUDITORIO',
    capacidad: 200,
    estado: 'MANTENIMIENTO',
  },
  {
    id: 3,
    nombre: 'Sala de reuniones 2B',
    descripcion: 'Sala para 8 personas',
    tipo: 'SALA_REUNIONES',
    capacidad: 8,
    estado: 'DISPONIBLE',
  },
  {
    id: 4,
    nombre: 'Proyector Epson X1',
    descripcion: 'Equipo audiovisual móvil',
    tipo: 'EQUIPO_AUDIOVISUAL',
    capacidad: 1,
    estado: 'FUERA_DE_SERVICIO',
  },
];

export const reservasMock: Reserva[] = [
  {
    id: 1,
    usuario: { id: 1, nombre: 'Ana García', estado: 'ACTIVO' },
    recurso: { id: 1, nombre: 'Lab 101', estado: 'DISPONIBLE' },
    fechaInicio: '2026-10-20T09:00:00',
    fechaFin: '2026-10-20T11:00:00',
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-14T10:00:00',
  },
  {
    id: 2,
    usuario: { id: 1, nombre: 'Ana García', estado: 'ACTIVO' },
    recurso: { id: 3, nombre: 'Sala de reuniones 2B', estado: 'DISPONIBLE' },
    fechaInicio: '2026-10-20T14:00:00',
    fechaFin: '2026-10-20T15:00:00',
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-14T11:00:00',
  },
];
