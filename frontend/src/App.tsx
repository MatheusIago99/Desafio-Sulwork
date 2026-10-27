import { useEffect, useState } from 'react';
import { BrowserRouter, NavLink, Route, Routes } from 'react-router-dom';
import { api } from './api/client';
import type { Participante } from './api/types';
import { EventoPage } from './pages/EventoPage';
import { EventosPage } from './pages/EventosPage';
import { ParticipantesPage } from './pages/ParticipantesPage';
import { ParticipanteAtualProvider, useParticipanteAtual } from './ParticipanteAtual';

function SeletorParticipanteAtual() {
  const { participanteId, setParticipanteId } = useParticipanteAtual();
  const [participantes, setParticipantes] = useState<Participante[]>([]);

  useEffect(() => {
    api.participantes.listar().then(setParticipantes).catch(() => setParticipantes([]));
  }, []);

  return (
    <label className="seletor-atual">
      Você é
      <select
        value={participanteId ?? ''}
        onFocus={() => api.participantes.listar().then(setParticipantes).catch(() => {})}
        onChange={(e) => setParticipanteId(e.target.value ? Number(e.target.value) : null)}
      >
        <option value="">Selecione...</option>
        {participantes.map((p) => (
          <option key={p.id} value={p.id}>
            {p.nome}
          </option>
        ))}
      </select>
    </label>
  );
}

export function App() {
  return (
    <ParticipanteAtualProvider>
      <BrowserRouter>
        <header className="topo">
          <div className="topo-conteudo">
            <NavLink to="/" className="marca">
              <span className="marca-icone" aria-hidden="true">
                S
              </span>
              <span>
                Sulwork <span className="marca-destaque">Eventos</span>
              </span>
            </NavLink>
            <nav className="menu">
              <NavLink to="/" end>
                Eventos
              </NavLink>
              <NavLink to="/participantes">Participantes</NavLink>
            </nav>
            <SeletorParticipanteAtual />
          </div>
        </header>
        <main>
          <Routes>
            <Route path="/" element={<EventosPage />} />
            <Route path="/eventos/:id" element={<EventoPage />} />
            <Route path="/participantes" element={<ParticipantesPage />} />
          </Routes>
        </main>
      </BrowserRouter>
    </ParticipanteAtualProvider>
  );
}
