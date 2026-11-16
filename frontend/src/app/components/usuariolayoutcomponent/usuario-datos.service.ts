import { Injectable, computed, signal } from '@angular/core';

/*
 * Servicio del apartado USUARIO.
 *
 * IMPORTANTE: por ahora todo es información de ejemplo (datos ficticios) que vive
 * solo en memoria. Cuando exista el backend en Spring Boot, estos métodos deben
 * reemplazarse por llamadas HTTP. El backend será el responsable de devolver
 * ÚNICAMENTE el historial del usuario autenticado (nunca el de otras personas).
 */

export type EstadoScraping = 'exitoso' | 'fallido';

export interface RespuestaIA {
  numero: 1 | 2 | 3;
  enfoque: string;
  resumen: string;
  hallazgos: string[];
  confianza: number; // 0 - 100
}

export interface ReportePdf {
  nombreArchivo: string;
  tamano: string;
  enviadoCorreo: boolean;
}

export interface AnalisisScraping {
  id: number;
  url: string;
  fecha: string; // ISO
  estado: EstadoScraping;
  duracionSegundos: number;
  motivoFallo?: string;
  descripcion: string;
  tema: string;
  informacionRelevante: string[];
  personas: string[];
  imagenes: number;
  tecnologias: string[];
  respuestasIA: RespuestaIA[];
  reporte: ReportePdf | null;
}

export interface PerfilUsuario {
  nombreCompleto: string;
  correo: string;
  nombreUsuario: string;
  rol: 'Usuario';
  miembroDesde: string; // ISO
}

export type TipoAviso = 'exito' | 'error' | 'info' | 'correo';

export interface Aviso {
  id: number;
  tipo: TipoAviso;
  titulo: string;
  mensaje: string;
  fecha: string; // ISO
  leido: boolean;
}

const RESPUESTAS_EJEMPLO: RespuestaIA[] = [
  {
    numero: 1,
    enfoque: 'Resumen general',
    resumen:
      'La página es una tienda en línea de productos ecológicos para el hogar. Presenta un catálogo, promociones vigentes y una sección institucional sobre la empresa.',
    hallazgos: ['Catálogo con 48 productos', 'Promoción de envío gratis', 'Formulario de contacto'],
    confianza: 92,
  },
  {
    numero: 2,
    enfoque: 'Contenido y personas',
    resumen:
      'Sitio comercial orientado a la sostenibilidad. Menciona al equipo fundador y testimonios de clientes; el tono es cercano y educativo.',
    hallazgos: ['3 personas del equipo identificadas', 'Blog con 12 artículos', 'Testimonios de clientes'],
    confianza: 87,
  },
  {
    numero: 3,
    enfoque: 'Tecnología del sitio',
    resumen:
      'El sitio parece construido con React y servido mediante Next.js. Usa una pasarela de pagos externa y carga imágenes optimizadas.',
    hallazgos: ['Framework: Next.js (React)', 'Estilos con Tailwind CSS', '34 imágenes optimizadas'],
    confianza: 81,
  },
];

@Injectable({ providedIn: 'root' })
export class UsuarioDatosService {
  private siguienteId = 107;
  private siguienteAvisoId = 10;

  /** Perfil del usuario actual (ficticio hasta que exista inicio de sesión). */
  readonly perfil = signal<PerfilUsuario>({
    nombreCompleto: 'Daniel Malagón',
    correo: 'daniel.malagon@correo.com',
    nombreUsuario: 'danielmalagon',
    rol: 'Usuario',
    miembroDesde: '2026-08-21T10:00:00',
  });

  /** Historial propio del usuario actual. */
  readonly historial = signal<AnalisisScraping[]>([
    this.crearEjemplo(106, 'https://www.tiendaverde.co', '2026-11-14T15:32:00', 'exitoso'),
    {
      ...this.crearEjemplo(105, 'https://portal-noticias-andinas.com/tecnologia', '2026-11-13T09:12:00', 'exitoso'),
      descripcion:
        'Portal de noticias regional con una sección dedicada a tecnología, startups y transformación digital.',
      tema: 'Noticias de tecnología',
      personas: ['Camilo Herrera', 'Natalia Pineda'],
      imagenes: 57,
      tecnologias: ['WordPress', 'PHP', 'jQuery'],
    },
    this.crearEjemplo(104, 'https://intranet.empresa-demo.local', '2026-11-12T18:45:00', 'fallido',
      'Tiempo de espera agotado: la página no respondió después de 30 segundos.'),
    {
      ...this.crearEjemplo(103, 'https://academia-codigo.org/cursos', '2026-11-10T11:05:00', 'exitoso'),
      descripcion: 'Plataforma educativa con cursos de programación para principiantes y nivel intermedio.',
      tema: 'Educación en programación',
      personas: ['Andrea Salcedo', 'Mateo Quintero', 'Valentina Ríos', 'Jorge Castaño'],
      imagenes: 22,
      tecnologias: ['Angular', 'TypeScript', 'Bootstrap'],
    },
    this.crearEjemplo(102, 'https://sitio-protegido.net', '2026-11-08T16:20:00', 'fallido',
      'Acceso denegado (HTTP 403): el sitio bloquea la navegación automatizada.'),
    {
      ...this.crearEjemplo(101, 'https://restaurantelacasona.com.co', '2026-11-05T13:40:00', 'exitoso'),
      descripcion: 'Página de un restaurante con menú, reservas en línea y galería de platos.',
      tema: 'Gastronomía',
      personas: ['Chef Ricardo Gómez'],
      imagenes: 41,
      tecnologias: ['Wix', 'JavaScript'],
    },
  ]);

  readonly avisos = signal<Aviso[]>([
    { id: 3, tipo: 'correo', titulo: 'Reporte enviado', mensaje: 'El reporte PDF de tiendaverde.co fue enviado a tu correo.', fecha: '2026-11-14T15:33:00', leido: false },
    { id: 2, tipo: 'exito', titulo: 'Scraping completado', mensaje: 'El análisis de tiendaverde.co terminó correctamente.', fecha: '2026-11-14T15:32:00', leido: false },
    { id: 1, tipo: 'error', titulo: 'Scraping fallido', mensaje: 'intranet.empresa-demo.local: tiempo de espera agotado.', fecha: '2026-11-12T18:45:00', leido: true },
  ]);

  /** Notificaciones emergentes (toasts) visibles en pantalla. */
  readonly toasts = signal<Aviso[]>([]);

  readonly totalExitosos = computed(() => this.historial().filter(a => a.estado === 'exitoso').length);
  readonly totalFallidos = computed(() => this.historial().filter(a => a.estado === 'fallido').length);
  readonly totalReportes = computed(() => this.historial().filter(a => a.reporte).length);
  readonly avisosSinLeer = computed(() => this.avisos().filter(a => !a.leido).length);

  obtenerAnalisis(id: number): AnalisisScraping | undefined {
    return this.historial().find(a => a.id === id);
  }

  /** Solo para la vista previa: agrega un análisis ficticio al historial en memoria. */
  registrarAnalisisDemo(url: string, exitoso: boolean): AnalisisScraping {
    const analisis = this.crearEjemplo(
      this.siguienteId++,
      url,
      new Date().toISOString(),
      exitoso ? 'exitoso' : 'fallido',
      exitoso ? undefined : 'No fue posible cargar la página: el servidor respondió con un error 500.',
    );
    this.historial.update(lista => [analisis, ...lista]);

    const dominio = this.dominio(url);
    if (exitoso) {
      this.notificar('exito', 'Scraping completado', `El análisis de ${dominio} terminó correctamente.`);
      this.notificar('correo', 'Reporte enviado', `El reporte PDF de ${dominio} fue enviado a ${this.perfil().correo}.`);
    } else {
      this.notificar('error', 'Scraping fallido', `${dominio}: ${analisis.motivoFallo}`);
    }
    return analisis;
  }

  actualizarPerfil(datos: Pick<PerfilUsuario, 'nombreCompleto' | 'correo' | 'nombreUsuario'>): void {
    this.perfil.update(p => ({ ...p, ...datos }));
  }

  descargarReporte(analisis: AnalisisScraping): void {
    // Pendiente: descargar el archivo real desde el backend.
    this.notificar('info', 'Descarga de reporte', `${analisis.reporte?.nombreArchivo} estará disponible al conectar el backend.`, false);
  }

  /** Muestra un toast y, opcionalmente, lo guarda en el centro de notificaciones. */
  notificar(tipo: TipoAviso, titulo: string, mensaje: string, guardar = true): void {
    const aviso: Aviso = { id: this.siguienteAvisoId++, tipo, titulo, mensaje, fecha: new Date().toISOString(), leido: false };
    if (guardar) {
      this.avisos.update(lista => [aviso, ...lista]);
    }
    this.toasts.update(lista => [...lista, aviso]);
    setTimeout(() => this.cerrarToast(aviso.id), 5000);
  }

  cerrarToast(id: number): void {
    this.toasts.update(lista => lista.filter(t => t.id !== id));
  }

  marcarAvisosLeidos(): void {
    this.avisos.update(lista => lista.map(a => ({ ...a, leido: true })));
  }

  dominio(url: string): string {
    try {
      return new URL(url).hostname.replace(/^www\./, '');
    } catch {
      return url;
    }
  }

  private crearEjemplo(id: number, url: string, fecha: string, estado: EstadoScraping, motivoFallo?: string): AnalisisScraping {
    const exitoso = estado === 'exitoso';
    return {
      id,
      url,
      fecha,
      estado,
      duracionSegundos: exitoso ? 24 : 30,
      motivoFallo,
      descripcion: exitoso
        ? 'Tienda en línea de productos ecológicos para el hogar, con catálogo, promociones, blog y sección institucional.'
        : '',
      tema: exitoso ? 'Comercio electrónico sostenible' : '',
      informacionRelevante: exitoso
        ? [
            'Ofrece envío gratis en compras superiores a $150.000.',
            'Cuenta con un blog sobre hábitos de consumo responsable.',
            'Publica datos de contacto y horario de atención al cliente.',
            'Tiene políticas de devolución y tratamiento de datos visibles.',
          ]
        : [],
      personas: exitoso ? ['María Fernanda López', 'Santiago Ruiz', 'Paula Andrea Gil'] : [],
      imagenes: exitoso ? 34 : 0,
      tecnologias: exitoso ? ['Next.js', 'React', 'Tailwind CSS'] : [],
      respuestasIA: exitoso ? RESPUESTAS_EJEMPLO : [],
      reporte: exitoso
        ? { nombreArchivo: `reporte-scraping-${id}.pdf`, tamano: '1,2 MB', enviadoCorreo: true }
        : null,
    };
  }
}
