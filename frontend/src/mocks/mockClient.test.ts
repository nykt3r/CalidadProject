// Comportamientos del mock usados por la vista Usuarios (F1):
// crear → aparece en tabla, DUPLICADO 409, 404 en detalle, cambio de estado.
import { describe, expect, it } from 'vitest';
import { ApiError } from '../api/client';
import { createMockApi } from './mockClient';

describe('mock de usuarios', () => {
  it('crear aparece en el listado', async () => {
    const mock = createMockApi();
    const antes = await mock.listarUsuarios();
    const creado = await mock.crearUsuario({
      nombre: 'Nuevo',
      documento: 'DOC-999',
      correo: 'nuevo@example.com',
    });
    expect(creado.estado).toBe('ACTIVO');
    const despues = await mock.listarUsuarios();
    expect(despues.length).toBe(antes.length + 1);
    expect(despues.some((u) => u.id === creado.id)).toBe(true);
  });

  it('duplicar documento o correo lanza DUPLICADO 409', async () => {
    const mock = createMockApi();
    await expect(
      mock.crearUsuario({ nombre: 'X', documento: 'DOC-001', correo: 'x@example.com' }),
    ).rejects.toMatchObject({ status: 409, error: 'DUPLICADO' });
    await expect(
      mock.crearUsuario({ nombre: 'X', documento: 'DOC-X', correo: 'ana@example.com' }),
    ).rejects.toMatchObject({ status: 409, error: 'DUPLICADO' });
  });

  it('detalle de id inexistente lanza NO_ENCONTRADO 404', async () => {
    const mock = createMockApi();
    await expect(mock.obtenerUsuario(9999)).rejects.toBeInstanceOf(ApiError);
    await expect(mock.obtenerUsuario(9999)).rejects.toMatchObject({
      status: 404,
      error: 'NO_ENCONTRADO',
    });
  });

  it('inactivar cambia el estado visible', async () => {
    const mock = createMockApi();
    const actualizado = await mock.cambiarEstadoUsuario(1, 'INACTIVO');
    expect(actualizado.estado).toBe('INACTIVO');
    expect((await mock.obtenerUsuario(1)).estado).toBe('INACTIVO');
  });
});

describe('mock de recursos', () => {
  it('CRUD completo: crear → editar → consultar', async () => {
    const mock = createMockApi();
    const creado = await mock.crearRecurso({
      nombre: 'Sala X',
      descripcion: 'Sala demo',
      tipo: 'SALA_REUNIONES',
      capacidad: 6,
    });
    expect(creado.estado).toBe('DISPONIBLE');
    const editado = await mock.modificarRecurso(creado.id, {
      nombre: 'Sala X',
      descripcion: 'Sala demo',
      tipo: 'SALA_REUNIONES',
      capacidad: 10,
    });
    expect(editado.capacidad).toBe(10);
    expect((await mock.obtenerRecurso(creado.id)).capacidad).toBe(10);
  });

  it('rechaza capacidad no positiva con VALIDACION 400', async () => {
    const mock = createMockApi();
    await expect(
      mock.crearRecurso({ nombre: 'X', descripcion: '', tipo: 'OTRO', capacidad: 0 }),
    ).rejects.toMatchObject({ status: 400, error: 'VALIDACION' });
  });

  it('filtros por estado y tipo reducen el listado', async () => {
    const mock = createMockApi();
    const todos = await mock.listarRecursos();
    const disponibles = await mock.listarRecursos({ estado: 'DISPONIBLE' });
    expect(disponibles.length).toBeLessThan(todos.length);
    expect(disponibles.every((r) => r.estado === 'DISPONIBLE')).toBe(true);
    const labs = await mock.listarRecursos({ tipo: 'LABORATORIO' });
    expect(labs.length).toBeGreaterThan(0);
    expect(labs.every((r) => r.tipo === 'LABORATORIO')).toBe(true);
  });

  it('cambiar estado se refleja en la consulta', async () => {
    const mock = createMockApi();
    await mock.cambiarEstadoRecurso(1, 'MANTENIMIENTO');
    expect((await mock.obtenerRecurso(1)).estado).toBe('MANTENIMIENTO');
  });

  it('disponibilidad muestra ocupado con solapadas y libre sin ellas', async () => {
    const mock = createMockApi();
    const ocupada = await mock.consultarDisponibilidad(1, '2026-10-20T09:00:00', '2026-10-20T11:00:00');
    expect(ocupada.disponible).toBe(false);
    expect(ocupada.reservasSolapadas.length).toBeGreaterThan(0);
    const libre = await mock.consultarDisponibilidad(1, '2026-10-21T09:00:00', '2026-10-21T11:00:00');
    expect(libre.disponible).toBe(true);
    expect(libre.reservasSolapadas).toEqual([]);
    const enMantenimiento = await mock.consultarDisponibilidad(2, '2026-10-21T09:00:00', '2026-10-21T10:00:00');
    expect(enMantenimiento.disponible).toBe(false);
  });
});

describe('mock de reservas', () => {
  const hueco = { usuarioId: 1, recursoId: 3, fechaInicio: '2026-10-22T09:00:00', fechaFin: '2026-10-22T10:00:00' };

  it('crear en hueco libre deja estado ACTIVA', async () => {
    const mock = createMockApi();
    const creada = await mock.crearReserva(hueco);
    expect(creada.estado).toBe('ACTIVA');
    expect((await mock.obtenerReserva(creada.id)).id).toBe(creada.id);
  });

  it('cada regla de negocio devuelve su código', async () => {
    const casos: Array<{ cuerpo: { usuarioId: number; recursoId: number; fechaInicio: string; fechaFin: string }; error: string }> = [
      { cuerpo: { ...hueco, usuarioId: 2 }, error: 'USUARIO_INACTIVO' },
      { cuerpo: { ...hueco, recursoId: 2 }, error: 'RECURSO_EN_MANTENIMIENTO' },
      { cuerpo: { ...hueco, recursoId: 4 }, error: 'RECURSO_FUERA_DE_SERVICIO' },
      {
        cuerpo: { usuarioId: 1, recursoId: 1, fechaInicio: '2026-10-20T10:00:00', fechaFin: '2026-10-20T12:00:00' },
        error: 'RESERVA_SOLAPADA',
      },
    ];
    for (const { cuerpo, error } of casos) {
      const mock = createMockApi();
      await expect(mock.crearReserva(cuerpo)).rejects.toMatchObject({ status: 409, error });
    }
    // VALIDACION (regla 4) viaja con 400, se aserta aparte.
    const mock = createMockApi();
    await expect(
      mock.crearReserva({ ...hueco, fechaInicio: '2026-10-22T10:00:00', fechaFin: '2026-10-22T09:00:00' }),
    ).rejects.toMatchObject({ status: 400, error: 'VALIDACION' });
  });

  it('con 3 activas la cuarta lanza LIMITE_RESERVAS_ACTIVAS', async () => {
    const mock = createMockApi();
    await mock.crearReserva(hueco); // 3.ª activa del usuario 1 (ya trae 2)
    await expect(mock.crearReserva({ ...hueco, fechaInicio: '2026-10-23T09:00:00', fechaFin: '2026-10-23T10:00:00' })).rejects.toMatchObject({
      status: 409,
      error: 'LIMITE_RESERVAS_ACTIVAS',
    });
  });

  it('cancelar cambia a CANCELADA y recancelar avisa RESERVA_FINALIZADA', async () => {
    const mock = createMockApi();
    const cancelada = await mock.cancelarReserva(1);
    expect(cancelada.estado).toBe('CANCELADA');
    await expect(mock.cancelarReserva(1)).rejects.toMatchObject({
      status: 409,
      error: 'RESERVA_FINALIZADA',
    });
  });

  it('consultas por usuario, recurso y fecha', async () => {
    const mock = createMockApi();
    expect((await mock.reservasPorUsuario(1)).length).toBe(2);
    expect((await mock.reservasPorUsuario(1, 'ACTIVA')).length).toBe(2);
    expect((await mock.reservasPorRecurso(1)).length).toBe(1);
    expect((await mock.reservasPorFecha('2026-10-20')).length).toBe(2);
    expect((await mock.reservasPorFecha('2026-10-21')).length).toBe(0);
    await expect(mock.obtenerReserva(9999)).rejects.toMatchObject({ status: 404, error: 'NO_ENCONTRADO' });
  });
});
