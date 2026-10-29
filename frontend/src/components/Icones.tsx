/** Ícones de traço simples (24×24), herdam a cor do texto. */
const base = {
  width: 18,
  height: 18,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.8,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
  'aria-hidden': true,
};

export const IconeInicio = () => (
  <svg {...base}>
    <path d="M3 10.5 12 3l9 7.5" />
    <path d="M5 9.5V21h14V9.5" />
    <path d="M10 21v-6h4v6" />
  </svg>
);

export const IconeCalendario = () => (
  <svg {...base}>
    <rect x="3" y="4.5" width="18" height="16.5" rx="2.5" />
    <path d="M3 9.5h18M8 2.5v4M16 2.5v4" />
  </svg>
);

export const IconePessoas = () => (
  <svg {...base}>
    <circle cx="9" cy="8" r="3.5" />
    <path d="M2.5 20c.6-3.6 3.2-5.5 6.5-5.5s5.9 1.9 6.5 5.5" />
    <path d="M16 4.8a3.3 3.3 0 0 1 0 6.4M18.5 14.8c1.7.8 2.8 2.5 3 5.2" />
  </svg>
);

export const IconeLista = () => (
  <svg {...base}>
    <path d="M9 6h11M9 12h11M9 18h11" />
    <path d="m3.5 6 1.2 1.2L7 4.8M3.5 12l1.2 1.2L7 10.8M3.5 18l1.2 1.2L7 16.8" />
  </svg>
);

export const IconeBusca = () => (
  <svg {...base}>
    <circle cx="11" cy="11" r="7" />
    <path d="m20 20-3.5-3.5" />
  </svg>
);

export const IconeMais = () => (
  <svg {...base}>
    <path d="M12 5v14M5 12h14" />
  </svg>
);

export const IconeSeta = () => (
  <svg {...base}>
    <path d="m9 6 6 6-6 6" />
  </svg>
);

export const IconeCheck = () => (
  <svg {...base}>
    <circle cx="12" cy="12" r="9" />
    <path d="m8 12.5 2.5 2.5L16 9.5" />
  </svg>
);

export const IconeAlerta = () => (
  <svg {...base}>
    <path d="M12 8v5M12 16.5v.5" />
    <path d="M10.3 3.9 2.6 17.5A2 2 0 0 0 4.3 20.5h15.4a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z" />
  </svg>
);

export const IconeGrafico = () => (
  <svg {...base}>
    <path d="M12 3a9 9 0 1 0 9 9h-9V3Z" />
    <path d="M15 3.5A9 9 0 0 1 20.5 9H15V3.5Z" />
  </svg>
);

export const IconeRelogio = () => (
  <svg {...base}>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v5l3 2" />
  </svg>
);

export const IconeCaixa = () => (
  <svg {...base}>
    <path d="M3.5 7.5 12 3l8.5 4.5v9L12 21l-8.5-4.5v-9Z" />
    <path d="M3.5 7.5 12 12l8.5-4.5M12 12v9" />
  </svg>
);

export const IconeAmpulheta = () => (
  <svg {...base}>
    <path d="M6.5 3h11M6.5 21h11" />
    <path d="M7.5 3c0 4.5 4.5 5.5 4.5 9s-4.5 4.5-4.5 9M16.5 3c0 4.5-4.5 5.5-4.5 9s4.5 4.5 4.5 9" />
  </svg>
);

/* Ícones dos tipos de evento, usados grandes nas capas ilustrativas */
type PropsTamanho = { tamanho?: number };
const grande = (tamanho = 18) => ({ ...base, width: tamanho, height: tamanho, strokeWidth: tamanho > 30 ? 1.2 : 1.8 });

export const IconeXicara = ({ tamanho }: PropsTamanho) => (
  <svg {...grande(tamanho)}>
    <path d="M4 9h12v5a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5V9Z" />
    <path d="M16 10.5h1.5a2.5 2.5 0 0 1 0 5H16M8 3.5c-.6.9.6 1.6 0 2.5M12 3.5c-.6.9.6 1.6 0 2.5M3 21.5h14" />
  </svg>
);

export const IconeTalheres = ({ tamanho }: PropsTamanho) => (
  <svg {...grande(tamanho)}>
    <path d="M6 3v6a2 2 0 0 0 4 0V3M8 11v10M17 21V3c-2 1.5-3 4-3 7v3h3" />
  </svg>
);

export const IconeLanche = ({ tamanho }: PropsTamanho) => (
  <svg {...grande(tamanho)}>
    <path d="M4 11a8 6 0 0 1 16 0H4Z" />
    <path d="M3.5 14h17M5 17h14a1 1 0 0 1 0 2.5H5A1 1 0 0 1 5 17Z" />
  </svg>
);

export const IconeChama = ({ tamanho }: PropsTamanho) => (
  <svg {...grande(tamanho)}>
    <path d="M12 21.5c-3.9 0-6.5-2.7-6.5-6.2 0-3.9 3.3-5.6 3.8-9.8 2.6 1.6 4 3.8 4.2 6 .9-.6 1.5-1.6 1.7-2.8 1.9 1.8 3.3 4.2 3.3 6.6 0 3.5-2.6 6.2-6.5 6.2Z" />
  </svg>
);

export const IconeTacas = ({ tamanho }: PropsTamanho) => (
  <svg {...grande(tamanho)}>
    <path d="M5 3h5l-.5 5a2 2 0 0 1-4 0L5 3ZM7.5 10v8M5 21h5M14 3h5l.5 5a2 2 0 0 1-4 0L14 3ZM16.5 10v8M14 21h5" />
  </svg>
);

export const IconeEstrela = ({ tamanho }: PropsTamanho) => (
  <svg {...grande(tamanho)}>
    <path d="m12 3 2.7 5.6 6.1.8-4.4 4.3 1 6.1L12 17l-5.4 2.8 1-6.1-4.4-4.3 6.1-.8L12 3Z" />
  </svg>
);
