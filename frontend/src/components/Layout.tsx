import { NavLink } from 'react-router';
import { modoApi } from '../api/api';
import './Layout.css';

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div className="app">
      <header className="app-header">
        <strong>Reservas</strong>
        <nav>
          <NavLink to="/usuarios">Usuarios</NavLink>
          <NavLink to="/recursos">Recursos</NavLink>
          <NavLink to="/reservas">Reservas</NavLink>
          <NavLink to="/disponibilidad">Disponibilidad</NavLink>
        </nav>
        <span className="modo" title="Origen de datos">
          {modoApi === 'mock' ? 'mock' : 'real'}
        </span>
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}
