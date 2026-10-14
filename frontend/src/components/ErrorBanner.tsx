import { esApiError } from '../api/client';

export function mensajeDeError(valor: unknown): string {
  if (esApiError(valor)) return `${valor.error}: ${valor.message}`;
  if (valor instanceof Error) return valor.message;
  return 'Error inesperado';
}

export default function ErrorBanner({ error, onReintentar }: { error: unknown; onReintentar?: () => void }) {
  if (error === null || error === undefined) return null;
  return (
    <div className="banner-error" role="alert">
      <span>{mensajeDeError(error)}</span>
      {onReintentar && (
        <button type="button" onClick={onReintentar}>
          Reintentar
        </button>
      )}
    </div>
  );
}
