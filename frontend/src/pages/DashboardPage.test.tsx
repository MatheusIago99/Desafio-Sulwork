import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { Dashboard } from '../api/types';
import { DashboardPage } from './DashboardPage';

function evento(id: number, nome: string) {
  return {
    id,
    nome,
    descricao: null,
    tipo: 'CAFE_DA_MANHA' as const,
    dataHora: '2026-11-03T08:00:00',
    prazoAlteracoes: '2026-11-01T18:00:00',
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

const base: Dashboard = {
  eventosAbertos: 1,
  eventosEmOrganizacao: 2,
  participantesCadastrados: 5,
  inscricoesEmEventosAbertos: 4,
  coberturaMedia: 35,
  obrigatoriosPendentes: 3,
  eventosPorTipo: [
    { tipo: 'CAFE_DA_MANHA', quantidade: 2 },
    { tipo: 'CHURRASCO', quantidade: 1 },
  ],
  proximosEventos: [
    { evento: evento(1, 'Café de boas-vindas'), inscritos: 4, percentualCobertura: 35, obrigatoriosPendentes: 3, pronto: false },
  ],
};

describe('DashboardPage', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('mostra indicadores reais e os próximos eventos com cobertura', async () => {
    responder(base);
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    const indicadores = await screen.findByRole('region', { name: 'Indicadores' });
    expect(within(indicadores).getByText('35%')).toBeInTheDocument();
    expect(within(indicadores).getByText('2 em organização')).toBeInTheDocument();
    expect(within(indicadores).getByText('3')).toBeInTheDocument();

    const proximo = screen.getByRole('link', { name: /Café de boas-vindas/ });
    expect(proximo).toHaveAttribute('href', '/eventos/1');
    expect(within(proximo).getByText('3 obrigatório(s) sem dono')).toBeInTheDocument();
    expect(within(proximo).getByRole('progressbar')).toHaveAttribute('aria-valuenow', '35');
  });

  it('não inventa cobertura quando não há eventos abertos', async () => {
    responder({ ...base, eventosAbertos: 0, coberturaMedia: null, proximosEventos: [] });
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(await screen.findByText('—')).toBeInTheDocument();
    expect(screen.getByText('nenhum evento aberto')).toBeInTheDocument();
    expect(screen.getByText(/Nenhum evento por vir/)).toBeInTheDocument();
  });
});
