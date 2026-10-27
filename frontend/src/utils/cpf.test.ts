import { cpfValido, mascararCpf, somenteDigitos } from './cpf';

describe('cpf', () => {
  it.each(['52998224725', '529.982.247-25', '111.444.777-35'])('aceita CPF válido %s', (cpf) => {
    expect(cpfValido(cpf)).toBe(true);
  });

  it.each(['52998224724', '5299822472', '11111111111', '', 'abc'])('rejeita CPF inválido "%s"', (cpf) => {
    expect(cpfValido(cpf)).toBe(false);
  });

  it('aplica a máscara progressivamente', () => {
    expect(mascararCpf('529')).toBe('529');
    expect(mascararCpf('5299')).toBe('529.9');
    expect(mascararCpf('5299822')).toBe('529.982.2');
    expect(mascararCpf('52998224725')).toBe('529.982.247-25');
    expect(mascararCpf('529982247251234')).toBe('529.982.247-25');
  });

  it('remove a máscara', () => {
    expect(somenteDigitos('529.982.247-25')).toBe('52998224725');
  });
});
