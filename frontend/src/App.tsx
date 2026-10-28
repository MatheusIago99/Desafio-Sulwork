import { useEffect, useState } from 'react';
import { BrowserRouter, NavLink, Route, Routes } from 'react-router-dom';
import { api } from './api/client';
import type { Participante } from './api/types';
import { IconeCalendario, IconeInicio, IconeLista, IconePessoas } from './components/Icones';
import { DashboardPage } from './pages/DashboardPage';
import { EventoPage } from './pages/EventoPage';
import { EventosPage } from './pages/EventosPage';
import { MinhasContribuicoesPage } from './pages/MinhasContribuicoesPage';
import { ParticipantesPage } from './pages/ParticipantesPage';
import { ParticipanteAtualProvider, useParticipanteAtual } from './ParticipanteAtual';
import { iniciais } from './utils/iniciais';

function SeletorParticipanteAtual() {
  const { participanteId, setParticipanteId } = useParticipanteAtual();
  const [participantes, setParticipantes] = useState<Participante[]>([]);
  const atual = participantes.find((p) => p.id === participanteId);

  const carregar = () => api.participantes.listar().then(setParticipantes).catch(() => {});
  useEffect(() => {
    void carregar();
  }, []);

  return (
    <div className="usuario-atual">
      <span className="avatar" aria-hidden="true">
        {atual ? iniciais(atual.nome) : '?'}
      </span>
      <label className="usuario-atual-campo">
        <span>Você é</span>
        <select
          aria-label="Você é"
          value={participanteId ?? ''}
          onFocus={carregar}
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
    </div>
  );
}

export function App() {
  return (
    <ParticipanteAtualProvider>
      <BrowserRouter>
        <div className="layout">
          <aside className="lateral">
            <NavLink to="/" className="marca">
              <span className="marca-icone" aria-hidden="true">
                S
              </span>
              <span>
                Sulwork <span className="marca-destaque">Eventos</span>
              </span>
            </NavLink>
            <nav className="menu" aria-label="Principal">
              <NavLink to="/" end>
                <IconeInicio /> Dashboard
              </NavLink>
              <NavLink to="/eventos">
                <IconeCalendario /> Eventos
              </NavLink>
              <NavLink to="/minhas-contribuicoes">
                <IconeLista /> Minhas contribuições
              </NavLink>
              <NavLink to="/participantes">
                <IconePessoas /> Participantes
              </NavLink>
            </nav>
            <SeletorParticipanteAtual />
          </aside>
          <main>
            <Routes>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/eventos" element={<EventosPage />} />
              <Route path="/eventos/:id" element={<EventoPage />} />
              <Route path="/minhas-contribuicoes" element={<MinhasContribuicoesPage />} />
              <Route path="/participantes" element={<ParticipantesPage />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </ParticipanteAtualProvider>
  );
}
