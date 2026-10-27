/** Remove tudo que não for dígito. */
export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

/** Aplica a máscara 000.000.000-00 progressivamente, enquanto o usuário digita. */
export function mascararCpf(valor: string): string {
  const d = somenteDigitos(valor).slice(0, 11);
  if (d.length <= 3) return d;
  if (d.length <= 6) return `${d.slice(0, 3)}.${d.slice(3)}`;
  if (d.length <= 9) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`;
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
}

function digitoVerificador(digitos: string, quantidade: number): number {
  let soma = 0;
  for (let i = 0; i < quantidade; i++) {
    soma += Number(digitos[i]) * (quantidade + 1 - i);
  }
  const resto = (soma * 10) % 11;
  return resto === 10 ? 0 : resto;
}

/** Mesma regra do backend: 11 dígitos, não repetidos, com dígitos verificadores corretos. */
export function cpfValido(valor: string): boolean {
  const d = somenteDigitos(valor);
  if (d.length !== 11 || /^(\d)\1{10}$/.test(d)) return false;
  return digitoVerificador(d, 9) === Number(d[9]) && digitoVerificador(d, 10) === Number(d[10]);
}
