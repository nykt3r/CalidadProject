import { Navigate, Route, Routes } from 'react-router';
import Layout from './components/Layout';
import DisponibilidadPage from './pages/DisponibilidadPage';
import RecursosPage from './pages/RecursosPage';
import ReservasPage from './pages/ReservasPage';
import UsuarioDetallePage from './pages/UsuarioDetallePage';
import UsuariosPage from './pages/UsuariosPage';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/usuarios" replace />} />
        <Route path="/usuarios" element={<UsuariosPage />} />
        <Route path="/usuarios/:id" element={<UsuarioDetallePage />} />
        <Route path="/recursos" element={<RecursosPage />} />
        <Route path="/reservas" element={<ReservasPage />} />
        <Route path="/disponibilidad" element={<DisponibilidadPage />} />
        <Route path="*" element={<p>Página no encontrada.</p>} />
      </Routes>
    </Layout>
  );
}
