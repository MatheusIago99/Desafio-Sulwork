import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { MinhaContribuicao } from '../api/types';
import { ParticipanteAtualProvider } from '../ParticipanteAtual';
import { MinhasContribuicoesPage } from './MinhasContribuicoesPage';

function contribuicao(sobrescrever: Partial<MinhaContribuicao>): MinhaContribuicao {
  return {
    contribuicaoId: 1,
    eventoId: 1,
    eventoNome: 'Café',
    eventoDataHora: '2099-11-04T08:00:00',
    prazoAlteracoes: '2099-11-02T18:00:00',
    eventoStatus: 'ABERTO',
    prazoVencido: false,
    aceitaAlteracoes: true,
    itemId: 10,
    itemNome: 'Pão',
    unidade: 'unidades',
    obrigatorio: true,
    quantidade: 3,
    quantidadeNecessaria: 10,
    quantidadeContribuidaTotal: 8,
    ...sobrescrever,
  };
}

function renderizar(lista: MinhaContribuicao[], participanteId: number | null = 7) {
  if (participanteId !== null) localStorage.setItem('sulwork.participanteAtual', String(participanteId));
  const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(lista), { status: 200 }));
  vi.stubGlobal('fetch', fetchMock);
  render(
    <ParticipanteAtualProvider>
      <MemoryRouter>
        <MinhasContribuicoesPage />
      </MemoryRouter>
    </ParticipanteAtualProvider>,
  );
  return fetchMock;
}

describe('MinhasContribuicoesPage', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('pede para escolher quem é você quando ninguém está selecionado', () => {
    const fetchMock = renderizar([], null);

    expect(screen.getByText(/Selecione seu nome no campo "Você é"/)).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('agrupa as contribuições do participante por evento', async () => {
    const fetchMock = renderizar([
      contribuicao({ contribuicaoId: 1, eventoId: 1, eventoNome: 'Café', itemNome: 'Pão' }),
      contribuicao({ contribuicaoId: 2, eventoId: 1, eventoNome: 'Café', itemNome: 'Suco', unidade: 'litros', quantidade: 2 }),
      contribuicao({ contribuicaoId: 3, eventoId: 2, eventoNome: 'Almoço', itemNome: 'Arroz', unidade: 'kg', quantidade: 1 }),
    ]);

    const cafe = await screen.findByRole('region', { name: 'Café' });
    expect(fetchMock).toHaveBeenCalledWith('/api/participantes/7/contribuicoes', expect.anything());
    expect(screen.getByText('3 itens em 2 eventos.')).toBeInTheDocument();
    expect(within(cafe).getAllByRole('row')).toHaveLength(3); // cabeçalho + 2 itens
    expect(within(cafe).getByText('2 litros')).toBeInTheDocument();
    expect(within(screen.getByRole('region', { name: 'Almoço' })).getByText('Arroz')).toBeInTheDocument();
  });

  it('bloqueia alterar e remover quando o evento não aceita mais alterações', async () => {
    renderizar([contribuicao({ prazoVencido: true, aceitaAlteracoes: false })]);

    const cafe = await screen.findByRole('region', { name: 'Café' });
    expect(within(cafe).getByText('Aberto · prazo encerrado')).toBeInTheDocument();
    expect(within(cafe).getByText(/Alterações encerradas/)).toBeInTheDocument();
    expect(within(cafe).queryByRole('button', { name: 'Alterar' })).not.toBeInTheDocument();
    expect(within(cafe).queryByRole('button', { name: 'Remover' })).not.toBeInTheDocument();
  });

  it('mostra um caminho quando a pessoa ainda não assumiu nada', async () => {
    renderizar([]);

    expect(await screen.findByText(/ainda não assumiu nenhum item/)).toBeInTheDocument();
  });
});
