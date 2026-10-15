INSERT INTO usuarios (nombre, documento, correo, estado) VALUES ('Ana García', 'DOC-001', 'ana@example.com', 'ACTIVO');
INSERT INTO usuarios (nombre, documento, correo, estado) VALUES ('Luis Pérez', 'DOC-002', 'luis@example.com', 'INACTIVO');
INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado) VALUES ('Lab 101', 'Laboratorio de cómputo', 'LABORATORIO', 30, 'DISPONIBLE');
INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado) VALUES ('Auditorio Central', 'Auditorio principal', 'AUDITORIO', 200, 'MANTENIMIENTO');
INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado) VALUES ('Sala 201', 'Sala de reuniones pequeña', 'SALA_REUNIONES', 10, 'FUERA_DE_SERVICIO');
INSERT INTO reservas (usuario_id, recurso_id, fecha_inicio, fecha_fin, estado, fecha_creacion) VALUES (1, 1, '2026-10-20 09:00:00', '2026-10-20 11:00:00', 'ACTIVA', '2026-10-14 10:00:00');
