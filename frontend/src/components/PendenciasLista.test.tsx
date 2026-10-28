import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { itemPendente, pendencias } from '../test/fixtures';
import { PendenciasLista } from './PendenciasLista';

describe('PendenciasLista ("O que falta?")', () => {
  it('mostra os itens na ordem recebida, com prioridade, quantidade faltante e conflitos', () => {
    render(
      <PendenciasLista
        pendencias={pendencias([
          itemPendente({
            itemId: 1,
            nome: 'Bolo',
            alergenos: ['LACTOSE'],
            conflitos: [{ alergeno: 'LACTOSE', participantes: ['Ana'] }],
            seguroParaTodos: false,
          }),
          itemPendente({ itemId: 2, nome: 'Suco', obrigatorio: false, prioridade: 'BAIXA', quantidadeFaltante: 3, unidade: 'litros' }),
        ])}
        participanteSelecionado
        participanteInscrito
        onAssumir={vi.fn()}
      />,
    );

    const itens = screen.getAllByTestId('pendencia');
    expect(itens[0]).toHaveTextContent('Bolo');
    expect(itens[0]).toHaveTextContent('Obrigatório');
    expect(itens[0]).toHaveTextContent('Lactose: restrição de Ana');
    expect(itens[1]).toHaveTextContent('Suco');
    expect(itens[1]).toHaveTextContent('faltam 3 litros');
    expect(screen.getByText(/1 obrigatório/)).toBeInTheDocument();
  });

  it('"Eu levo" assume tudo o que falta', async () => {
    const onAssumir = vi.fn().mockResolvedValue(undefined);
    const item = itemPendente({ quantidadeFaltante: 2 });
    render(
      <PendenciasLista pendencias={pendencias([item])} participanteSelecionado participanteInscrito onAssumir={onAssumir} />,
    );

    await userEvent.click(screen.getByRole('button', { name: /Eu levo os 2/ }));

    expect(onAssumir).toHaveBeenCalledWith(item, undefined);
  });

  it('permite levar só parte da quantidade, validando o máximo', async () => {
    const onAssumir = vi.fn().mockResolvedValue(undefined);
    const item = itemPendente({ quantidadeFaltante: 5 });
    render(
      <PendenciasLista pendencias={pendencias([item])} participanteSelecionado participanteInscrito onAssumir={onAssumir} />,
    );
    const campo = screen.getByLabelText(/Quantidade de/);
    const botao = screen.getByRole('button', { name: 'Levar só essa quantidade' });

    await userEvent.type(campo, '9');
    expect(botao).toBeDisabled();

    await userEvent.clear(campo);
    await userEvent.type(campo, '2');
    await userEvent.click(botao);
    expect(onAssumir).toHaveBeenCalledWith(item, 2);
  });

  it.each([
    [false, true, true, 'não está aceitando contribuições'],
    [true, false, false, 'Selecione quem é você'],
    [true, true, false, 'Inscreva-se no evento'],
  ])(
    'bloqueia a ação e explica o motivo (aceita=%s, selecionado=%s, inscrito=%s)',
    (aceita, selecionado, inscrito, motivo) => {
      render(
        <PendenciasLista
          pendencias={pendencias([itemPendente()], aceita)}
          participanteSelecionado={selecionado}
          participanteInscrito={inscrito}
          onAssumir={vi.fn()}
        />,
      );

      expect(screen.getByText(new RegExp(motivo))).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Eu levo/ })).toBeDisabled();
    },
  );

  it('informa quando nada falta', () => {
    render(<PendenciasLista pendencias={pendencias([])} participanteSelecionado participanteInscrito onAssumir={vi.fn()} />);
    expect(screen.getByText(/já têm responsável/)).toBeInTheDocument();
  });
});
