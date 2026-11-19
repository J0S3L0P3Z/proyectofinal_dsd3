// La usan crear cuenta y el perfil del usuario, para no tener la misma función dos veces
export function calcularFuerza(clave: string): { puntos: number; texto: string } {
  if (!clave) {
    return { puntos: 0, texto: '' };
  }

  let puntos = 0;
  if (clave.length >= 6) puntos++;
  if (clave.length >= 10) puntos++;
  if (/[A-Z]/.test(clave) && /[0-9]/.test(clave)) puntos++;
  if (/[^A-Za-z0-9]/.test(clave)) puntos++;

  let texto = 'Contraseña excelente';
  if (puntos <= 1) texto = 'Contraseña muy débil';
  else if (puntos === 2) texto = 'Contraseña moderada';
  else if (puntos === 3) texto = 'Buena contraseña';

  return { puntos, texto };
}
