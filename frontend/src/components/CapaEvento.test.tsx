import { render, screen } from '@testing-library/react';
import { CapaEvento } from './CapaEvento';

describe('CapaEvento', () => {
  it('se apresenta como ilustração, não como foto, e mostra tipo e status', () => {
    render(<CapaEvento tipo="CHURRASCO" status="ABERTO" />);
    expect(screen.getByRole('img', { name: 'Capa ilustrativa: Churrasco' })).toBeInTheDocument();
    expect(screen.getByText('Churrasco')).toBeInTheDocument();
    expect(screen.getByText('Aberto')).toBeInTheDocument();
  });

  it('mantém o aviso de prazo encerrado no selo da capa', () => {
    render(<CapaEvento tipo="ALMOCO" status="ABERTO" prazoVencido />);
    expect(screen.getByText('Aberto · prazo encerrado')).toBeInTheDocument();
  });
});
