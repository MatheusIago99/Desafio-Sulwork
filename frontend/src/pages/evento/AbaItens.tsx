import { useState, type FormEvent } from 'react';
import { api } from '../../api/client';
import type { Alergeno, Categoria, Item } from '../../api/types';
import { AlergenoChips, SeletorAlergenos } from '../../components/Alergenos';
import { BarraProgresso } from '../../components/BarraProgresso';
import { Modal } from '../../components/Modal';
import { QUANTIDADE_MAXIMA, formatarQuantidade } from '../../utils/format';
import type { DadosEvento, Executar } from './contexto';

export function AbaItens({ dados, executar }: { dados: DadosEvento; executar: Executar }) {
  const { evento, categorias, itens } = dados;
  const podeOrganizar = evento.status === 'RASCUNHO' || evento.status === 'ABERTO';
  const [itemEditando, setItemEditando] = useState<Item | 'novo' | null>(null);

  return (
    <div>
      {podeOrganizar && <FormCategoria eventoId={evento.id} executar={executar} />}
      {categorias.length === 0 && (
        <p className="vazio">Crie uma categoria (ex.: Bebidas, Salgados) para começar a cadastrar itens.</p>
      )}

      {podeOrganizar && categorias.length > 0 && (
        <button type="button" className="botao botao-primario" onClick={() => setItemEditando('novo')}>
          Novo item
        </button>
      )}
      {itemEditando !== null && (
        <Modal
          titulo={itemEditando === 'novo' ? 'Novo item' : `Editar ${itemEditando.nome}`}
          onFechar={() => setItemEditando(null)}
          lateral
        >
          <FormItem
            key={itemEditando === 'novo' ? 'novo' : itemEditando.id}
            eventoId={evento.id}
            categorias={categorias}
            inicial={itemEditando === 'novo' ? undefined : itemEditando}
            executar={executar}
            onFim={() => setItemEditando(null)}
          />
        </Modal>
      )}

      {categorias.map((categoria) => {
        const itensDaCategoria = itens.filter((i) => i.categoria.id === categoria.id);
        return (
          <section key={categoria.id} className="categoria">
            <CabecalhoCategoria
              eventoId={evento.id}
              categoria={categoria}
              podeOrganizar={podeOrganizar}
              vazia={itensDaCategoria.length === 0}
              executar={executar}
            />
            {itensDaCategoria.length === 0 ? (
              <p className="texto-suave">Sem itens.</p>
            ) : (
              <div className="tabela-container">
                <table className="tabela">
                  <thead>
                    <tr>
                      <th>Item</th>
                      <th>Progresso</th>
                      <th>Alérgenos</th>
                      {podeOrganizar && <th aria-label="Ações" />}
                    </tr>
                  </thead>
                  <tbody>
                    {itensDaCategoria.map((item) => (
                      <tr key={item.id}>
                        <td>
                          <strong>{item.nome}</strong>{' '}
                          <span className={`badge ${item.obrigatorio ? 'badge-prioridade-alta' : 'badge-neutro'}`}>
                            {item.obrigatorio ? 'Obrigatório' : 'Opcional'}
                          </span>
                        </td>
                        <td className="coluna-progresso">
                          <span>
                            {item.quantidadeContribuida} de {formatarQuantidade(item.quantidadeNecessaria, item.unidade)}
                          </span>
                          <BarraProgresso
                            percentual={(item.quantidadeContribuida / item.quantidadeNecessaria) * 100}
                            rotulo={`Progresso de ${item.nome}`}
                            pequena
                          />
                        </td>
                        <td>
                          <AlergenoChips alergenos={item.alergenos} vazio="Nenhum" />
                        </td>
                        {podeOrganizar && (
                          <td className="acoes-tabela">
                            <button type="button" className="botao botao-pequeno" onClick={() => setItemEditando(item)}>
                              Editar
                            </button>
                            <button
                              type="button"
                              className="botao botao-pequeno botao-perigo"
                              onClick={() =>
                                confirm(`Excluir ${item.nome}?`) &&
                                executar(() => api.itens.excluir(evento.id, item.id), 'Item excluído.')
                              }
                            >
                              Excluir
                            </button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        );
      })}
    </div>
  );
}

function CabecalhoCategoria({
  eventoId,
  categoria,
  podeOrganizar,
  vazia,
  executar,
}: {
  eventoId: number;
  categoria: Categoria;
  podeOrganizar: boolean;
  vazia: boolean;
  executar: Executar;
}) {
  const [editando, setEditando] = useState(false);
  const [nome, setNome] = useState(categoria.nome);
  const [meta, setMeta] = useState(categoria.meta?.toString() ?? '');

  if (editando) {
    const salvar = async (e: FormEvent) => {
      e.preventDefault();
      const ok = await executar(() =>
        api.categorias.atualizar(eventoId, categoria.id, { nome, meta: meta ? Number(meta) : null }),
      );
      if (ok) setEditando(false);
    };
    return (
      <form className="linha-form" onSubmit={salvar}>
        <input aria-label="Nome da categoria" required value={nome} onChange={(e) => setNome(e.target.value)} />
        <input aria-label="Meta" type="number" min={1} max={QUANTIDADE_MAXIMA} placeholder="Meta" value={meta} onChange={(e) => setMeta(e.target.value)} />
        <button type="submit" className="botao botao-pequeno botao-primario">
          Salvar
        </button>
        <button type="button" className="botao botao-pequeno" onClick={() => setEditando(false)}>
          Cancelar
        </button>
      </form>
    );
  }

  return (
    <div className="linha-entre">
      <h3>
        {categoria.nome}{' '}
        {categoria.meta !== null && <span className="texto-suave">· meta de {categoria.meta} unidades</span>}
      </h3>
      {podeOrganizar && (
        <span className="acoes-tabela">
          <button type="button" className="botao botao-pequeno" onClick={() => setEditando(true)}>
            Editar categoria
          </button>
          {vazia && (
            <button
              type="button"
              className="botao botao-pequeno botao-perigo"
              onClick={() => executar(() => api.categorias.excluir(eventoId, categoria.id), 'Categoria excluída.')}
            >
              Excluir
            </button>
          )}
        </span>
      )}
    </div>
  );
}

function FormCategoria({ eventoId, executar }: { eventoId: number; executar: Executar }) {
  const [nome, setNome] = useState('');
  const [meta, setMeta] = useState('');

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await executar(
      () => api.categorias.criar(eventoId, { nome, meta: meta ? Number(meta) : null }),
      'Categoria criada.',
    );
    if (ok) {
      setNome('');
      setMeta('');
    }
  };

  return (
    <form className="linha-form" onSubmit={enviar}>
      <input
        aria-label="Nova categoria"
        placeholder="Nova categoria (ex.: Bebidas)"
        required
        maxLength={60}
        value={nome}
        onChange={(e) => setNome(e.target.value)}
      />
      <input
        aria-label="Meta da categoria"
        type="number"
        min={1}
        max={QUANTIDADE_MAXIMA}
        placeholder="Meta (opcional)"
        title="Quantidade mínima de unidades que a categoria deve reunir"
        value={meta}
        onChange={(e) => setMeta(e.target.value)}
      />
      <button type="submit" className="botao">
        Adicionar categoria
      </button>
    </form>
  );
}

function FormItem({
  eventoId,
  categorias,
  inicial,
  executar,
  onFim,
}: {
  eventoId: number;
  categorias: Categoria[];
  inicial?: Item;
  executar: Executar;
  onFim: () => void;
}) {
  const [nome, setNome] = useState(inicial?.nome ?? '');
  const [categoriaId, setCategoriaId] = useState(inicial?.categoria.id ?? categorias[0]?.id);
  const [quantidade, setQuantidade] = useState(inicial?.quantidadeNecessaria.toString() ?? '1');
  const [unidade, setUnidade] = useState(inicial?.unidade ?? '');
  const [obrigatorio, setObrigatorio] = useState(inicial?.obrigatorio ?? true);
  const [alergenos, setAlergenos] = useState<Alergeno[]>(inicial?.alergenos ?? []);
  const [erro, setErro] = useState<string | null>(null);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const dados = {
      nome,
      categoriaId: Number(categoriaId),
      quantidadeNecessaria: Number(quantidade),
      unidade,
      obrigatorio,
      alergenos,
    };
    // O erro aparece dentro do painel: o aviso da página fica atrás do fundo escurecido.
    try {
      await (inicial ? api.itens.atualizar(eventoId, inicial.id, dados) : api.itens.criar(eventoId, dados));
    } catch (ex) {
      setErro(ex instanceof Error ? ex.message : 'Erro inesperado');
      return;
    }
    await executar(async () => {}, inicial ? 'Item atualizado.' : 'Item criado.');
    onFim();
  };

  return (
    <form className="formulario" onSubmit={enviar}>
      <label>
        Nome
        <input required maxLength={80} value={nome} onChange={(e) => setNome(e.target.value)} />
      </label>
      <label>
        Categoria
        <select value={categoriaId} onChange={(e) => setCategoriaId(Number(e.target.value))}>
          {categorias.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome}
            </option>
          ))}
        </select>
      </label>
      <label>
        Quantidade necessária
        <input type="number" min={inicial?.quantidadeContribuida || 1} max={QUANTIDADE_MAXIMA} required value={quantidade} onChange={(e) => setQuantidade(e.target.value)} />
      </label>
      <label>
        Unidade (opcional)
        <input maxLength={20} placeholder="ex.: litros, pacotes" value={unidade} onChange={(e) => setUnidade(e.target.value)} />
      </label>
      <label className="checkbox campo-largo">
        <input type="checkbox" checked={obrigatorio} onChange={(e) => setObrigatorio(e.target.checked)} />
        Item obrigatório (o evento não fica pronto sem ele)
      </label>
      <div className="campo-largo">
        <SeletorAlergenos legenda="Alérgenos que o item contém" selecionados={alergenos} onChange={setAlergenos} />
      </div>
      {erro && (
        <p className="texto-erro campo-largo" role="alert">
          {erro}
        </p>
      )}
      <div className="acoes campo-largo">
        <button type="submit" className="botao botao-primario">
          {inicial ? 'Salvar item' : 'Adicionar item'}
        </button>
        <button type="button" className="botao" onClick={onFim}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
