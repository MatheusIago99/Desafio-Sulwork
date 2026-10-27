import { createContext, useContext, useState, type ReactNode } from 'react';

const CHAVE = 'sulwork.participanteAtual';

interface ParticipanteAtualContexto {
  participanteId: number | null;
  setParticipanteId: (id: number | null) => void;
}

const Contexto = createContext<ParticipanteAtualContexto>({
  participanteId: null,
  setParticipanteId: () => {},
});

function lerSalvo(): number | null {
  try {
    const valor = localStorage.getItem(CHAVE);
    return valor ? Number(valor) : null;
  } catch {
    return null;
  }
}

/**
 * A aplicação não tem autenticação (premissa documentada no README): o usuário escolhe
 * "quem é você" no topo da página, e essa escolha é usada nas ações de contribuição.
 */
export function ParticipanteAtualProvider({ children }: { children: ReactNode }) {
  const [participanteId, setId] = useState<number | null>(lerSalvo);

  const setParticipanteId = (id: number | null) => {
    setId(id);
    try {
      if (id === null) localStorage.removeItem(CHAVE);
      else localStorage.setItem(CHAVE, String(id));
    } catch {
      // armazenamento indisponível: a escolha vale só para esta sessão
    }
  };

  return <Contexto.Provider value={{ participanteId, setParticipanteId }}>{children}</Contexto.Provider>;
}

export function useParticipanteAtual() {
  return useContext(Contexto);
}
