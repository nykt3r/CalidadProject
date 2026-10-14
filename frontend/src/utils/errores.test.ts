import { describe, expect, it } from 'vitest';
import { sugerenciaPara } from './errores';

describe('sugerenciaPara', () => {
  it('da sugerencia para cada código de negocio del catálogo', () => {
    for (const codigo of [
      'RESERVA_SOLAPADA',
      'LIMITE_RESERVAS_ACTIVAS',
      'USUARIO_INACTIVO',
      'RECURSO_EN_MANTENIMIENTO',
      'RECURSO_FUERA_DE_SERVICIO',
      'RESERVA_FINALIZADA',
      'DUPLICADO',
      'NO_ENCONTRADO',
    ]) {
      const sugerencia = sugerenciaPara(codigo);
      expect(sugerencia, codigo).toBeTruthy();
    }
  });

  it('devuelve null para códigos desconocidos', () => {
    expect(sugerenciaPara('OTRO')).toBeNull();
  });
});
