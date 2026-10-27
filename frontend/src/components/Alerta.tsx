import type { ReactNode } from 'react';

type Tipo = 'erro' | 'aviso' | 'info' | 'sucesso';

export function Alerta({ tipo, children, onFechar }: { tipo: Tipo; children: ReactNode; onFechar?: () => void }) {
  return (
    <div className={`alerta alerta-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      <div>{children}</div>
      {onFechar && (
        <button type="button" className="botao-icone" onClick={onFechar} aria-label="Fechar">
          ×
        </button>
      )}
    </div>
  );
}
