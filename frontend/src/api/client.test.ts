import { ApiError, api } from './client';

function respostaJson(status: number, corpo: unknown) {
  return new Response(JSON.stringify(corpo), { status, headers: { 'Content-Type': 'application/json' } });
}

describe('api client', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('envia JSON e devolve o corpo da resposta', async () => {
    const fetchMock = vi.fn().mockResolvedValue(respostaJson(201, { id: 7 }));
    vi.stubGlobal('fetch', fetchMock);

    const resultado = await api.inscricoes.inscrever(1, 2);

    expect(resultado).toEqual({ id: 7 });
    expect(fetchMock).toHaveBeenCalledWith('/api/eventos/1/inscricoes', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ participanteId: 2 }),
    }));
  });

  it('converte o erro padronizado do backend em ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respostaJson(400, {
      status: 400,
      mensagem: 'Dados inválidos',
      campos: [{ campo: 'cpf', mensagem: 'CPF inválido' }],
    })));

    const erro = await api.participantes.criar({ nome: 'A', cpf: '1', restricoes: [], observacoesAlimentares: '' })
      .catch((e: unknown) => e);

    expect(erro).toBeInstanceOf(ApiError);
    expect((erro as ApiError).status).toBe(400);
    expect((erro as ApiError).detalhada()).toBe('Dados inválidos: cpf CPF inválido');
  });

  it('trata 204 sem corpo', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })));
    await expect(api.eventos.excluir(1)).resolves.toBeUndefined();
  });

  it('dá uma mensagem clara quando a API está fora do ar', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));
    await expect(api.eventos.listar()).rejects.toThrow('Não foi possível conectar à API');
  });
});
