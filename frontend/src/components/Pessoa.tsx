import { iniciais } from '../utils/iniciais';

/** Nome com o avatar de iniciais, usado nas listas de pessoas. */
export function Pessoa({ nome, destaque = false }: { nome: string; destaque?: boolean }) {
  return (
    <span className="pessoa">
      <span className="avatar avatar-pequeno" aria-hidden="true">
        {iniciais(nome)}
      </span>
      {destaque ? <strong>{nome}</strong> : nome}
    </span>
  );
}
