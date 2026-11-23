import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from '@relynn/ngx-toastr';

import { Scrapingservice, ScrapingDTO } from '../../services/scrapingservice';
import { Resultadoservice, ResultadoDTO } from '../../services/resultadoservice';
import { Reporteservice, ReporteDTO } from '../../services/reporteservice';

/**
 * Pantalla con el resultado de un análisis: datos de la página, respuestas de las IA y reporte PDF.
 */
@Component({
  selector: 'app-resultadocomponent',
  standalone: false,
  styleUrl: './resultadocomponent.css',
  templateUrl: './resultadocomponent.html'
})
export class Resultadocomponent implements OnInit {

  /** Scraping que se está mostrando. */
  analisis: ScrapingDTO | null = null;
  /** Respuestas de las IA para el scraping. */
  resultados: ResultadoDTO[] = [];
  /** Reporte PDF del scraping, si tiene. */
  reporte: ReporteDTO | null = null;

  /** Indica si se está cargando el análisis. */
  cargando = true;
  /** Indica si el análisis no existe o no se pudo consultar. */
  noEncontrado = false;

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param route Ruta activa, de donde se lee el identificador del análisis
   * @param scrapingService Servicio de scrapings
   * @param resultadoService Servicio de resultados de las IA
   * @param reporteService Servicio de reportes PDF
   * @param toastr Servicio para mostrar notificaciones
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private route: ActivatedRoute,
    private scrapingService: Scrapingservice,
    private resultadoService: Resultadoservice,
    private reporteService: Reporteservice,
    private toastr: ToastrService
  ) {}

  /** Lee el identificador del análisis de la ruta y lo carga. */
  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const id = Number(
        params.get('idScraping') || this.route.snapshot.paramMap.get('id')
      );

      if (!id || isNaN(id)) {
        this.cargando = false;
        this.noEncontrado = true;
        return;
      }

      this.cargarAnalisis(id);
    });
  }

  /**
   * Consulta el scraping y, si existe, sus resultados y su reporte.
   *
   * @param id Identificador del scraping
   */
  cargarAnalisis(id: number): void {
    this.cargando = true;
    this.noEncontrado = false;
    this.analisis = null;
    this.resultados = [];
    this.reporte = null;

    this.scrapingService.buscarPorId(id).subscribe({

      next: (respuesta: ScrapingDTO) => {

        this.cambios.markForCheck();
        this.analisis = respuesta;
        this.cargando = false;

        this.cargarResultados(id);
        this.cargarReporte(id);
      },

      error: () => {

        this.cambios.markForCheck();
        this.cargando = false;
        this.noEncontrado = true;

        this.toastr.error(
          'No fue posible encontrar el análisis solicitado',
          'Error'
        );
      }
    });
  }

  /**
   * Consulta las respuestas de las IA para el scraping.
   *
   * @param idScraping Identificador del scraping
   */
  cargarResultados(idScraping: number): void {
    this.resultadoService.buscarPorScraping(idScraping).subscribe({

      next: (respuesta: ResultadoDTO[]) => {

        this.cambios.markForCheck();
        this.resultados = respuesta || [];
      },

      error: () => {

        this.cambios.markForCheck();
        this.toastr.error(
          'No fue posible consultar los resultados de las IA',
          'Error'
        );
      }
    });
  }

  /**
   * Consulta el reporte PDF del scraping.
   *
   * @param idScraping Identificador del scraping
   */
  cargarReporte(idScraping: number): void {
    this.reporteService.buscarPorScraping(idScraping).subscribe({

      next: (respuesta: ReporteDTO[]) => {

        this.cambios.markForCheck();
        this.reporte = respuesta && respuesta.length > 0 ? respuesta[0] : null;
      },

      error: () => {

        this.cambios.markForCheck();
        this.reporte = null;
      }
    });
  }

  /**
   * Separa los nombres de personas identificados por la IA.
   *
   * @returns Lista de nombres
   */
  obtenerPersonas(): string[] {
    const nombres = this.analisis?.nombresPersonas;

    if (!nombres) {
      return [];
    }

    return nombres.split(',').map(nombre => nombre.trim()).filter(nombre => nombre.length > 0);
  }

  /**
   * Separa las tecnologías identificadas por la IA.
   *
   * @returns Lista de tecnologías
   */
  obtenerTecnologias(): string[] {
    const tecnologias = this.analisis?.tecnologiasIdentificadas;

    if (!tecnologias) {
      return [];
    }

    return tecnologias.split(',').map(tecnologia => tecnologia.trim()).filter(tecnologia => tecnologia.length > 0);
  }

  /**
   * Indica si el análisis terminó bien (estado COMPLETADO).
   *
   * @returns true si el análisis fue exitoso
   */
  esExitoso(): boolean {
    const estado = (this.analisis?.estado || '').toUpperCase();

    return [
      'EXITO',
      'EXITOSO',
      'COMPLETADO',
      'COMPLETADA',
      'SUCCESS'
    ].includes(estado);
  }

  /** Descarga el reporte PDF del análisis y lo guarda en el equipo del usuario. */
  descargarReporte(): void {

    if (!this.reporte?.idReporte) {
      this.toastr.warning(
        'Este análisis no tiene un reporte disponible',
        'Aviso'
      );
      return;
    }

    this.reporteService.descargarPDF(this.reporte.idReporte).subscribe({

      next: (archivo: Blob) => {

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
}
