import { useState, type FormEvent } from 'react';
import type { Participante, ParticipanteInput } from '../api/types';
import { cpfValido, mascararCpf, somenteDigitos } from '../utils/cpf';
import { SeletorAlergenos } from './Alergenos';

interface Props {
  inicial?: Participante;
  onSalvar: (dados: ParticipanteInput) => Promise<void>;
  onCancelar: () => void;
}

export function ParticipanteForm({ inicial, onSalvar, onCancelar }: Props) {
  const [nome, setNome] = useState(inicial?.nome ?? '');
  const [cpf, setCpf] = useState(inicial ? inicial.cpfMascarado : '');
  const [restricoes, setRestricoes] = useState(inicial?.restricoes ?? []);
  const [observacoes, setObservacoes] = useState(inicial?.observacoesAlimentares ?? '');
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  // Na edição o campo mostra o CPF mascarado (desabilitado) e não é validado nem enviado.
  const cpfCompleto = !inicial && somenteDigitos(cpf).length === 11;
  const cpfInvalido = cpfCompleto && !cpfValido(cpf);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (!inicial && !cpfValido(cpf)) {
      setErro('Informe um CPF válido com 11 dígitos.');
      return;
    }
    setErro(null);
    setSalvando(true);
    try {
      await onSalvar({ nome: nome.trim(), cpf: somenteDigitos(cpf), restricoes, observacoesAlimentares: observacoes });
    } catch (ex) {
      setErro(ex instanceof Error ? ex.message : 'Erro ao salvar');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <form className="formulario" onSubmit={enviar} aria-label="Cadastro de participante">
      <label>
        Nome
        <input required maxLength={120} value={nome} onChange={(e) => setNome(e.target.value)} />
      </label>
      <div className="campo">
        <label htmlFor="participante-cpf">CPF</label>
        <input
          id="participante-cpf"
          required
          inputMode="numeric"
          placeholder="000.000.000-00"
          value={cpf}
          disabled={inicial !== undefined}
          aria-invalid={cpfInvalido}
          aria-describedby="participante-cpf-ajuda"
          onChange={(e) => setCpf(mascararCpf(e.target.value))}
        />
        <small id="participante-cpf-ajuda" className={cpfInvalido ? 'texto-erro' : 'texto-suave'}>
          {cpfInvalido ? 'CPF inválido' : inicial ? 'O CPF identifica o colaborador e não pode ser alterado.' : ''}
        </small>
      </div>
      <div className="campo-largo">
        <SeletorAlergenos legenda="Restrições alimentares (alérgenos)" selecionados={restricoes} onChange={setRestricoes} />
      </div>
      <label className="campo-largo">
        Outras restrições ou observações
        <input
          maxLength={500}
          placeholder="Ex.: vegetariano, não come carne de porco"
          value={observacoes}
          onChange={(e) => setObservacoes(e.target.value)}
        />
      </label>
      {erro && (
        <p className="texto-erro campo-largo" role="alert">
          {erro}
        </p>
      )}
      <div className="acoes campo-largo">
        <button type="submit" className="botao botao-primario" disabled={salvando}>
          {inicial ? 'Salvar' : 'Cadastrar'}
        </button>
        <button type="button" className="botao" onClick={onCancelar}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
