import { useState, type FormEvent } from 'react';
import type { Evento, EventoInput, TipoEvento } from '../api/types';
import { TIPO_LABEL, paraInputDataHora } from '../utils/format';

interface Props {
  inicial?: Evento;
  onSalvar: (dados: EventoInput) => Promise<void>;
  onCancelar: () => void;
}

export function EventoForm({ inicial, onSalvar, onCancelar }: Props) {
  const [dados, setDados] = useState<EventoInput>({
    nome: inicial?.nome ?? '',
    descricao: inicial?.descricao ?? '',
    tipo: inicial?.tipo ?? 'CAFE_DA_MANHA',
    dataHora: inicial ? paraInputDataHora(inicial.dataHora) : '',
    prazoAlteracoes: inicial ? paraInputDataHora(inicial.prazoAlteracoes) : '',
  });
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  const alterar = <K extends keyof EventoInput>(campo: K, valor: EventoInput[K]) =>
    setDados((atual) => ({ ...atual, [campo]: valor }));

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (dados.prazoAlteracoes && dados.dataHora && dados.prazoAlteracoes > dados.dataHora) {
      setErro('O prazo para alterações não pode ser depois do evento.');
      return;
    }
    setErro(null);
    setSalvando(true);
    try {
      await onSalvar({ ...dados, dataHora: `${dados.dataHora}:00`, prazoAlteracoes: `${dados.prazoAlteracoes}:00` });
    } catch (ex) {
      setErro(ex instanceof Error ? ex.message : 'Erro ao salvar');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <form className="formulario" onSubmit={enviar}>
      <label>
        Nome
        <input required maxLength={120} value={dados.nome} onChange={(e) => alterar('nome', e.target.value)} />
      </label>
      <label>
        Tipo
        <select value={dados.tipo} onChange={(e) => alterar('tipo', e.target.value as TipoEvento)}>
          {Object.entries(TIPO_LABEL).map(([valor, rotulo]) => (
            <option key={valor} value={valor}>
              {rotulo}
            </option>
          ))}
        </select>
      </label>
      <label className="campo-largo">
        Descrição
        <textarea
          maxLength={1000}
          rows={2}
          value={dados.descricao}
          onChange={(e) => alterar('descricao', e.target.value)}
        />
      </label>
      <label>
        Data e horário do evento
        <input
          type="datetime-local"
          required
          value={dados.dataHora}
          onChange={(e) => alterar('dataHora', e.target.value)}
        />
      </label>
      <label>
        Prazo para alterações
        <input
          type="datetime-local"
          required
          value={dados.prazoAlteracoes}
          onChange={(e) => alterar('prazoAlteracoes', e.target.value)}
        />
        <small className="texto-suave">Até quando os participantes podem mudar inscrições e contribuições.</small>
      </label>
      {erro && (
        <p className="texto-erro campo-largo" role="alert">
          {erro}
        </p>
      )}
      <div className="acoes campo-largo">
        <button type="submit" className="botao botao-primario" disabled={salvando}>
          {inicial ? 'Salvar alterações' : 'Criar evento'}
        </button>
        <button type="button" className="botao" onClick={onCancelar}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
