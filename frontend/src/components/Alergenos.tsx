import type { Alergeno } from '../api/types';
import { ALERGENO_LABEL, ALERGENOS } from '../utils/format';

export function AlergenoChips({ alergenos, vazio }: { alergenos: Alergeno[]; vazio?: string }) {
  if (alergenos.length === 0) {
    return vazio ? <span className="texto-suave">{vazio}</span> : null;
  }
  return (
    <span className="chips">
      {alergenos.map((a) => (
        <span key={a} className="chip chip-alergeno">
          {ALERGENO_LABEL[a]}
        </span>
      ))}
    </span>
  );
}

interface SeletorProps {
  legenda: string;
  selecionados: Alergeno[];
  onChange: (alergenos: Alergeno[]) => void;
}

export function SeletorAlergenos({ legenda, selecionados, onChange }: SeletorProps) {
  const alternar = (alergeno: Alergeno) => {
    onChange(
      selecionados.includes(alergeno) ? selecionados.filter((a) => a !== alergeno) : [...selecionados, alergeno],
    );
  };
  return (
    <fieldset className="seletor-alergenos">
      <legend>{legenda}</legend>
      {ALERGENOS.map((a) => (
        <label key={a} className="checkbox-chip">
          <input type="checkbox" checked={selecionados.includes(a)} onChange={() => alternar(a)} />
          {ALERGENO_LABEL[a]}
        </label>
      ))}
    </fieldset>
  );
}
