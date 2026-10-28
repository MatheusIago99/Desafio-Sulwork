import { tempoRelativo } from './tempoRelativo';

const agora = new Date('2026-03-10T12:00:00');

describe('tempoRelativo', () => {
  it.each([
    ['2026-03-10T11:59:40', 'agora há pouco'],
    ['2026-03-10T11:45:00', 'há 15 min'],
    ['2026-03-10T09:00:00', 'há 3 h'],
    ['2026-03-09T11:00:00', 'há 1 dia'],
    ['2026-03-07T12:00:00', 'há 3 dias'],
  ])('%s -> "%s"', (iso, esperado) => {
    expect(tempoRelativo(iso, agora)).toBe(esperado);
  });
});
