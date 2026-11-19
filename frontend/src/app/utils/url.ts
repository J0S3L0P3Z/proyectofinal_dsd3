// Validaciones de URL que usan el scraping del usuario y el del admin

export function errorDeUrl(url: string): string {
  if (!url) {
    return 'Ingresa la URL de la página que deseas analizar.';
  }
  if (!/^https?:\/\/[^\s/$.?#].[^\s]*$/i.test(url)) {
    return 'La URL no es válida. Debe comenzar con http:// o https://';
  }
  return '';
}

export function dominioDe(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, '');
  } catch {
    return url;
  }
}

// Simulación mientras no hay backend: si el dominio no tiene punto (ej. http://intranet)
// o es .local, hacemos como si la página no hubiera respondido
export function paginaNoResponde(url: string): boolean {
  const dominio = dominioDe(url);
  return !dominio.includes('.') || dominio.endsWith('.local');
}
