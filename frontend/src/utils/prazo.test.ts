import { prazoProximo, tempoRestante } from './prazo';

const agora = new Date('2026-03-10T09:00:00');

describe('tempoRestante', () => {
  it.each([
    ['2026-03-13T09:00:00', 'Encerra em 3 dias'],
    ['2026-03-11T10:00:00', 'Encerra em 1 dia'],
    ['2026-03-10T14:30:00', 'Encerra em 5 h'],
    ['2026-03-10T09:20:00', 'Encerra em 20 min'],
    ['2026-03-10T09:00:30', 'Encerra em 1 min'],
  ])('prazo %s -> "%s"', (prazo, esperado) => {
    expect(tempoRestante(prazo, agora)).toBe(esperado);
  });

  it('retorna null quando o prazo já passou', () => {
    expect(tempoRestante('2026-03-10T09:00:00', agora)).toBeNull();
    expect(tempoRestante('2026-03-09T18:00:00', agora)).toBeNull();
  });

  it('considera próximo o prazo com menos de 24 horas', () => {
    expect(prazoProximo('2026-03-10T20:00:00', agora)).toBe(true);
    expect(prazoProximo('2026-03-12T20:00:00', agora)).toBe(false);
    expect(prazoProximo('2026-03-09T20:00:00', agora)).toBe(false);
  });
});
