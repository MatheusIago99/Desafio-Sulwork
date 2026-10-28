import { render, screen } from '@testing-library/react';
import { StatusBadge } from './StatusBadge';

describe('StatusBadge', () => {
  it('mostra o status normalmente', () => {
    render(<StatusBadge status="ABERTO" />);
    expect(screen.getByText('Aberto')).toBeInTheDocument();
  });

  it('deixa explícito quando o evento está aberto mas o prazo já venceu', () => {
    render(<StatusBadge status="ABERTO" prazoVencido />);
    expect(screen.getByText('Aberto · prazo encerrado')).toBeInTheDocument();
  });

  it('não muda os outros status mesmo com prazo vencido', () => {
    render(<StatusBadge status="ENCERRADO" prazoVencido />);
    expect(screen.getByText('Encerrado')).toBeInTheDocument();
  });
});
