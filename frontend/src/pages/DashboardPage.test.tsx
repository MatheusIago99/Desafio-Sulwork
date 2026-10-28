import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { Dashboard } from '../api/types';
import { DashboardPage } from './DashboardPage';

function evento(id: number, nome: string) {
  return {
    id,
    nome,
    descricao: 'Descrição',
    tipo: 'CAFE_DA_MANHA' as const,
    dataHora: '2099-11-03T08:00:00',
    prazoAlteracoes: '2099-11-01T18:00:00',
    status: 'ABERTO' as const,
    prazoVencido: false,
    aceitaAlteracoes: true,
    transicoesPermitidas: [],
    criadoEm: '2026-10-27T09:00:00',
  };
}

function responder(dashboard: Dashboard) {
  vi.stubGlobal(
    'fetch',
    vi.fn((url: string) =>
      Promise.resolve(new Response(JSON.stringify(url.endsWith('/dashboard') ? dashboard : []), { status: 200 })),
    ),
  );
}

function renderizar() {
  render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>,
  );
}

const base: Dashboard = {
  coberturaMedia: 35,
  obrigatoriosPendentes: 3,
  proximosEventosTotal: 4,
  proximosEventosNaSemana: 1,
  contribuicoesTotal: 12,
  ultimaContribuicaoEm: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  proximosEventos: [
    { evento: evento(1, 'Café de boas-vindas'), inscritos: 4, percentualCobertura: 35, obrigatoriosPendentes: 3, pronto: false },
  ],
  pendenciasPrioritarias: [
    { eventoId: 1, eventoNome: 'Café de boas-vindas', itemId: 9, itemNome: 'Copos', categoria: 'Descartáveis', quantidadeFaltante: 50, unidade: 'unidades' },
  ],
  contribuicoesRecentes: [
    { eventoId: 1, eventoNome: 'Café de boas-vindas', participanteNome: 'Ana Souza', itemNome: 'Pão', quantidade: 10, unidade: 'unidades', atualizadoEm: new Date().toISOString() },
  ],
};

describe('DashboardPage', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('mostra os indicadores calculados pela API', async () => {
    responder(base);
    renderizar();

    const indicadores = await screen.findByRole('region', { name: 'Indicadores' });
    expect(within(indicadores).getByText('4')).toBeInTheDocument();
    expect(within(indicadores).getByText('1 nesta semana')).toBeInTheDocument();
    expect(within(indicadores).getByText('12')).toBeInTheDocument();
    expect(within(indicadores).getByText('Última há 2 h')).toBeInTheDocument();
    expect(within(indicadores).getByText('35%')).toBeInTheDocument();
  });

  it('lista próximos eventos, pendências prioritárias e contribuições recentes', async () => {
    responder(base);
    renderizar();

    const proximos = await screen.findByRole('region', { name: 'Próximos eventos' });
    expect(within(proximos).getByRole('heading', { name: 'Café de boas-vindas' })).toBeInTheDocument();
    expect(within(proximos).getByRole('link', { name: /Ver evento/ })).toHaveAttribute('href', '/eventos/1');
    expect(within(proximos).getByRole('progressbar')).toHaveAttribute('aria-valuenow', '35');

    const pendencias = screen.getByRole('region', { name: 'Pendências prioritárias' });
    expect(within(pendencias).getByText('Copos')).toBeInTheDocument();
    expect(within(pendencias).getByText('Faltam 50 unidades')).toBeInTheDocument();
    expect(within(pendencias).getByRole('link', { name: 'Contribuir' })).toHaveAttribute('href', '/eventos/1');

    const recentes = screen.getByRole('region', { name: 'Contribuições recentes' });
    expect(within(recentes).getByText(/vai levar 10 unidades de Pão/)).toBeInTheDocument();
  });

  it('não inventa números quando não há dados', async () => {
    responder({
      ...base,
      coberturaMedia: null,
      obrigatoriosPendentes: 0,
      contribuicoesTotal: 0,
      ultimaContribuicaoEm: null,
      proximosEventos: [],
      pendenciasPrioritarias: [],
      contribuicoesRecentes: [],
    });
    renderizar();

    expect(await screen.findByText('—')).toBeInTheDocument();
    expect(screen.getByText('Nenhum evento aberto')).toBeInTheDocument();
    expect(screen.getByText('Nenhuma ainda')).toBeInTheDocument();
    expect(screen.getByText(/Nenhum evento por vir/)).toBeInTheDocument();
    expect(screen.getByText('Nenhuma contribuição registrada ainda.')).toBeInTheDocument();
  });
});
