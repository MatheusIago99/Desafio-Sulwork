import { useEffect, useRef, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

interface Props {
  titulo: string;
  onFechar: () => void;
  children: ReactNode;
  largo?: boolean;
}

/**
 * Janela modal acessível: foco inicial no conteúdo, Esc e clique no fundo fecham.
 * É renderizada no body (portal) para o fundo cobrir a tela inteira, mesmo que algum
 * ancestro crie um novo contexto de posicionamento.
 */
export function Modal({ titulo, onFechar, children, largo }: Props) {
  const painel = useRef<HTMLDivElement>(null);
  // Guardado em ref: quem usa o modal costuma passar uma função nova a cada render, e o
  // efeito abaixo deve rodar só ao abrir (senão o foco voltaria ao primeiro campo a cada tecla).
  const fechar = useRef(onFechar);
  fechar.current = onFechar;

  useEffect(() => {
    const anterior = document.activeElement as HTMLElement | null;
    painel.current?.querySelector<HTMLElement>('input, select, textarea')?.focus();
    const aoTeclar = (e: KeyboardEvent) => e.key === 'Escape' && fechar.current();
    document.addEventListener('keydown', aoTeclar);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', aoTeclar);
      document.body.style.overflow = '';
      anterior?.focus();
    };
  }, []);

  return createPortal(
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && onFechar()}>
      <div
        ref={painel}
        className={`modal ${largo ? 'modal-largo' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-titulo"
      >
        <div className="modal-cabecalho">
          <h2 id="modal-titulo">{titulo}</h2>
          <button type="button" className="botao-icone" onClick={onFechar} aria-label="Fechar">
            ×
          </button>
        </div>
        {children}
      </div>
    </div>,
    document.body,
  );
}
