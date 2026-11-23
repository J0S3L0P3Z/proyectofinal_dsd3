import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ToastrService } from '@relynn/ngx-toastr';
import { Scrapingservice, ScrapingDTO } from '../../services/scrapingservice';
import { Reporteservice, ReporteDTO } from '../../services/reporteservice';

/** Pantalla para analizar una página: valida la URL, ejecuta el scraping y genera el reporte PDF. */
@Component({
  selector: 'app-scrapingcomponent',
  standalone: false,
  templateUrl: './scrapingcomponent.html',
  styleUrl: './scrapingcomponent.css'
})
export class Scrapingcomponent implements OnInit {

  /** Usuario con sesión iniciada. */
  usuario: any = null;

  /** URL escrita por el usuario. */
  url = '';
  /** Mensaje de error de la URL, o vacío si es válida. */
  errorUrl = '';

  /** Estado del análisis en la pantalla. */
  estado: 'inactivo' | 'procesando' | 'exitoso' | 'error' = 'inactivo';

  /** Scraping que devolvió el backend. */
  resultado: ScrapingDTO | null = null;
  /** Reporte PDF generado para el scraping. */
  reporte: ReporteDTO | null = null;

  /** Indica si se está generando el reporte PDF. */
  cargandoReporte = false;

  /** Pasos del análisis que se muestran mientras se procesa. */
  pasos = [
    { texto: 'Validando la URL' },
    { texto: 'Extrayendo el contenido de la página' },
    { texto: 'Gemini: análisis general' },
    { texto: 'Grok: análisis general' },
    { texto: 'Claude: análisis general' },
    { texto: 'Gemini: identificación de tecnologías' },
    { texto: 'Gemini: identificación de nombres de personas' },
    { texto: 'Guardando los resultados del análisis' },
    { texto: 'Generando el reporte PDF' }
  ];

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param scrapingService Servicio de scrapings
   * @param reporteService Servicio de reportes PDF
   * @param toastr Servicio para mostrar notificaciones
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private scrapingService: Scrapingservice,
    private reporteService: Reporteservice,
    private toastr: ToastrService
  ) {}

  /** Carga el usuario al abrir la pantalla. */
  ngOnInit(): void {
    this.obtenerUsuario();
  }

  /**
   * Indica si el análisis se está procesando.
   *
   * @returns true mientras se espera la respuesta del backend
   */
  get ejecutando(): boolean {
    return this.estado === 'procesando';
  }

  /** Lee el usuario guardado al iniciar sesión. */
  obtenerUsuario(): void {
    const datos = localStorage.getItem('usuario');

    if (datos) {
      try {
        this.usuario = JSON.parse(datos);
      } catch {
        this.toastr.error(
          'No fue posible cargar los datos del usuario',
          'Error'
        );
      }
    }
  }

  /**
   * Valida la URL (obligatoria y con http:// o https://) y ejecuta el scraping.
   * Si termina bien, genera el reporte PDF.
   */
  iniciarScraping(): void {

    const urlValidar = this.url.trim();

    this.errorUrl = '';

    if (!urlValidar) {
      this.errorUrl = 'Ingresa la URL de la página que deseas analizar.';
      return;
    }

    try {
      const direccion = new URL(urlValidar);

      if (
        direccion.protocol !== 'http:' &&
        direccion.protocol !== 'https:'
      ) {
        this.errorUrl = 'La URL debe comenzar con http:// o https://.';
        return;
      }

    } catch {
      this.errorUrl = 'Ingresa una dirección URL válida.';
      return;
    }

    if (!this.usuario || !this.usuario.id) {
      this.toastr.error(
        'No se encontró el usuario que inició sesión',
        'Error'
      );
      return;
    }

    this.resultado = null;
    this.reporte = null;
    this.estado = 'procesando';

    this.scrapingService.realizarScraping({
      url: urlValidar,
      idUsuario: this.usuario.id
    }).subscribe({

      next: (respuesta) => {

        this.cambios.markForCheck();

        this.resultado = respuesta;
        this.estado = 'exitoso';

        this.toastr.success(
          'El análisis de la página terminó correctamente',
          'Scraping completado'
        );

        if (respuesta.idScraping) {
          this.generarReporte(respuesta.idScraping);
        }
      },

      error: (error) => {

        this.cambios.markForCheck();

        this.estado = 'error';

        this.toastr.error(
          error.error?.mensaje || 'No fue posible completar el scraping',
          'Error en el análisis'
        );
      }
    });
  }

  /**
   * Pide al backend que genere el reporte PDF del scraping.
   *
   * @param idScraping Identificador del scraping
   */
  generarReporte(idScraping: number): void {

    this.cargandoReporte = true;

    this.reporteService.generarPDF(idScraping).subscribe({

      next: (respuesta) => {

        this.cambios.markForCheck();

        this.reporte = respuesta;
        this.cargandoReporte = false;

        this.toastr.success(
          'El reporte PDF fue generado correctamente',
          'Reporte generado'
        );
      },

      error: () => {

        this.cambios.markForCheck();

        this.cargandoReporte = false;

        this.toastr.warning(
          'El scraping terminó, pero no fue posible generar el PDF',
          'Aviso'
        );
      }
    });
  }

  /** Descarga el reporte PDF generado y lo guarda en el equipo del usuario. */
  descargarReporte(): void {

    if (!this.reporte?.idReporte) {
      this.toastr.warning(
        'No hay un reporte disponible para descargar',
        'Aviso'
      );
      return;
    }

    this.reporteService.descargarPDF(this.reporte.idReporte).subscribe({

      next: (archivo) => {

        this.cambios.markForCheck();

        const urlArchivo = window.URL.createObjectURL(archivo);
        const enlace = document.createElement('a');

        enlace.href = urlArchivo;
        enlace.download = this.reporte?.nombreArchivo || 'reporte.pdf';

        enlace.click();

        window.URL.revokeObjectURL(urlArchivo);

        this.toastr.success(
          'El reporte se descargó correctamente',
          'Descarga exitosa'
        );
      },

      error: () => {

        this.cambios.markForCheck();
        this.toastr.error(
          'No fue posible descargar el reporte',
          'Error'
        );
      }
    });
  }

  /** Limpia la pantalla para hacer un análisis nuevo. */
  nuevo(): void {
    this.estado = 'inactivo';
    this.resultado = null;
    this.reporte = null;
    this.url = '';
    this.errorUrl = '';
    this.cargandoReporte = false;
  }
}
