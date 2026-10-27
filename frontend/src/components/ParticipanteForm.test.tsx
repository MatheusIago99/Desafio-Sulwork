import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ParticipanteForm } from './ParticipanteForm';

describe('ParticipanteForm', () => {
  it('mascara o CPF e aponta CPF inválido sem enviar', async () => {
    const onSalvar = vi.fn();
    render(<ParticipanteForm onSalvar={onSalvar} onCancelar={vi.fn()} />);

    await userEvent.type(screen.getByLabelText('Nome'), 'Ana');
    await userEvent.type(screen.getByLabelText('CPF'), '12345678900');

    expect(screen.getByLabelText('CPF')).toHaveValue('123.456.789-00');
    expect(screen.getByText('CPF inválido')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Cadastrar' }));
    expect(onSalvar).not.toHaveBeenCalled();
  });

  it('envia CPF sem máscara e as restrições marcadas', async () => {
    const onSalvar = vi.fn().mockResolvedValue(undefined);
    render(<ParticipanteForm onSalvar={onSalvar} onCancelar={vi.fn()} />);

    await userEvent.type(screen.getByLabelText('Nome'), '  Ana Souza ');
    await userEvent.type(screen.getByLabelText('CPF'), '52998224725');
    await userEvent.click(screen.getByLabelText('Lactose'));
    await userEvent.type(screen.getByLabelText(/Outras restrições/), 'Vegetariana');
    await userEvent.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(onSalvar).toHaveBeenCalledWith({
      nome: 'Ana Souza',
      cpf: '52998224725',
      restricoes: ['LACTOSE'],
      observacoesAlimentares: 'Vegetariana',
    });
  });

  it('exibe o erro devolvido pela API', async () => {
    const onSalvar = vi.fn().mockRejectedValue(new Error('Já existe um participante cadastrado com este CPF.'));
    render(<ParticipanteForm onSalvar={onSalvar} onCancelar={vi.fn()} />);

    await userEvent.type(screen.getByLabelText('Nome'), 'Ana');
    await userEvent.type(screen.getByLabelText('CPF'), '52998224725');
    await userEvent.click(screen.getByRole('button', { name: 'Cadastrar' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Já existe um participante');
  });
});
