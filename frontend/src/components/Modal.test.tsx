import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { Modal } from './Modal';

function ComCampo({ onFechar }: { onFechar: () => void }) {
  const [texto, setTexto] = useState('');
  return (
    <Modal titulo="Novo evento" onFechar={onFechar}>
      <input aria-label="Nome" value={texto} onChange={(e) => setTexto(e.target.value)} />
      <input aria-label="Outro" />
    </Modal>
  );
}

describe('Modal', () => {
  it('é um diálogo nomeado pelo título, com foco no primeiro campo', () => {
    render(<ComCampo onFechar={vi.fn()} />);

    expect(screen.getByRole('dialog', { name: 'Novo evento' })).toBeInTheDocument();
    expect(screen.getByLabelText('Nome')).toHaveFocus();
  });

  it('mantém o foco no campo enquanto o usuário digita', async () => {
    // Regressão: o pai recria onFechar a cada render, e o foco voltava ao primeiro campo a cada tecla.
    render(<ComCampo onFechar={() => {}} />);

    await userEvent.click(screen.getByLabelText('Outro'));
    await userEvent.type(screen.getByLabelText('Outro'), 'abc');

    expect(screen.getByLabelText('Outro')).toHaveFocus();
    expect(screen.getByLabelText('Outro')).toHaveValue('abc');
  });

  it('fecha com Esc e com o botão de fechar', async () => {
    const onFechar = vi.fn();
    render(<ComCampo onFechar={onFechar} />);

    await userEvent.keyboard('{Escape}');
    await userEvent.click(screen.getByRole('button', { name: 'Fechar' }));

    expect(onFechar).toHaveBeenCalledTimes(2);
  });
});
