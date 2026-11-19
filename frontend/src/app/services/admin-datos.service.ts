import {Injectable, inject} from '@angular/core';
import {SesionService} from './sesion.service';
import {RespuestaIA} from './usuario-datos.service';
import {dominioDe} from '../utils/url';

export interface ScrapingAdmin {
  id: number;
  url: string;
  dominio: string;
  fecha: string;
  exitoso: boolean;
  motivoFallo: string;
  adminCorreo: string;
  respuestas: RespuestaIA[];
}

// Los scrapings que hace cada admin se guardan en el localStorage hasta que exista el backend
const LLAVE_HISTORIAL = 'scrapingdoo_scraping_admin';

@Injectable({providedIn: 'root'})
export class AdminDatosService {

  private sesion = inject(SesionService);
  private historial: ScrapingAdmin[] = [];

  // url que manda el historial cuando se le da "Volver a analizar"
  urlPendiente = '';

  constructor() {
    const guardado = localStorage.getItem(LLAVE_HISTORIAL);
    if (guardado) {
      this.historial = JSON.parse(guardado);
    }
  }

  // Solo los del admin que tiene la sesión abierta
  misScrapings(): ScrapingAdmin[] {
    const correo = this.sesion.usuarioActual()?.correo;
    return this.historial.filter(s => s.adminCorreo === correo);
  }

  // Todos los admins, para las estadísticas
  todos(): ScrapingAdmin[] {
    return this.historial;
  }

  registrar(url: string, exitoso: boolean, respuestas: RespuestaIA[], motivoFallo = ''): ScrapingAdmin {
    let siguienteId = 1;
    for (const s of this.historial) {
      if (s.id >= siguienteId) siguienteId = s.id + 1;
    }

    const nuevo: ScrapingAdmin = {
      id: siguienteId,
      url,
      dominio: dominioDe(url),
      fecha: new Date().toISOString(),
      exitoso,
      motivoFallo,
      adminCorreo: this.sesion.usuarioActual()?.correo ?? '',
      respuestas,
    };

    this.historial.unshift(nuevo);
    this.guardar();
    return nuevo;
  }

  eliminar(id: number) {
    this.historial = this.historial.filter(s => s.id !== id);
    this.guardar();
  }

  // Respuestas de prueba de las 3 IAs mientras se conecta el backend
  respuestasDePrueba(url: string): RespuestaIA[] {
    const dominio = dominioDe(url);
    const azar = (min: number, max: number) => min + Math.floor(Math.random() * (max - min + 1));
    const tecnologias = ['Angular', 'React', 'WordPress', 'Vue', 'Laravel', 'Next.js', 'Bootstrap', 'Tailwind'];
    const tec1 = tecnologias[azar(0, 3)];
    const tec2 = tecnologias[azar(4, 7)];

    return [
      {
        numero: 1,
        enfoque: 'Resumen general',
        resumen: dominio + ' es un sitio con página de inicio, una sección sobre la empresa y un formulario de contacto. '
          + 'El contenido principal está pensado para mostrar sus servicios.',
        hallazgos: [azar(3, 9) + ' secciones en el menú', 'Formulario de contacto', 'Enlaces a redes sociales'],
        confianza: azar(80, 96),
      },
      {
        numero: 2,
        enfoque: 'Contenido y personas',
        resumen: 'En ' + dominio + ' aparecen nombres del equipo y algunos testimonios. '
          + 'Tiene imágenes de producto y textos cortos con un tono cercano.',
        hallazgos: [azar(0, 5) + ' personas mencionadas', azar(8, 60) + ' imágenes', 'Blog o sección de noticias'],
        confianza: azar(72, 92),
      },
      {
        numero: 3,
        enfoque: 'Tecnología del sitio',
        resumen: 'Por el código de la página, ' + dominio + ' parece estar hecho con ' + tec1 + ' y usa ' + tec2
          + ' para los estilos. Carga por HTTPS.',
        hallazgos: ['Framework: ' + tec1, 'Estilos: ' + tec2, 'Certificado HTTPS válido'],
        confianza: azar(78, 95),
      },
    ];
  }

  private guardar() {
    localStorage.setItem(LLAVE_HISTORIAL, JSON.stringify(this.historial));
  }
}
