import { render, screen } from '@testing-library/react';
import { cobertura } from '../test/fixtures';
import { CoberturaResumo } from './CoberturaResumo';

describe('CoberturaResumo', () => {
  it('mostra percentual geral, obrigatórios, metas e categorias consideradas', () => {
    render(
      <CoberturaResumo
        cobertura={cobertura({
          percentualGeral: 72,
          obrigatoriosTotal: 3,
          obrigatoriosCompletos: 2,
          metasTotal: 1,
          metasAtingidas: 0,
          categorias: [
            { id: 1, nome: 'Bebidas', meta: 10, totalNecessario: 8, totalContribuido: 6, percentual: 60, criterio: 'META', metaAtingivel: false, itens: [] },
            { id: 2, nome: 'Vazia', meta: null, totalNecessario: 0, totalContribuido: 0, percentual: 0, criterio: 'IGNORADA', metaAtingivel: true, itens: [] },
          ],
        })}
      />,
    );

    expect(screen.getByText('72%')).toBeInTheDocument();
    expect(screen.getByText('2/3')).toBeInTheDocument();
    expect(screen.getByText('0/1')).toBeInTheDocument();
    expect(screen.getByText('Ainda falta coisa')).toBeInTheDocument();
    expect(screen.getByText(/pela meta: 6\/10/)).toBeInTheDocument();
    expect(screen.getByText(/nunca será atingida/)).toBeInTheDocument();
    expect(screen.queryByText('Vazia')).not.toBeInTheDocument();
    expect(screen.getByRole('progressbar', { name: 'Cobertura geral' })).toHaveAttribute('aria-valuenow', '72');
  });

  it('indica quando o evento está pronto', () => {
    render(<CoberturaResumo cobertura={cobertura({ percentualGeral: 90, pronto: true })} />);
    expect(screen.getByText('Pronto para acontecer')).toBeInTheDocument();
  });
});
