import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ToastrService } from '@relynn/ngx-toastr';

import { Scrapingservice, ScrapingDTO } from '../../services/scrapingservice';
import { Historialservice, HistorialDTO } from '../../services/historialservice';
import { Resultadoservice } from '../../services/resultadoservice';
import { Reporteservice } from '../../services/reporteservice';

/** Pantalla de inicio del usuario con el resumen de sus análisis. */
@Component({
  selector: 'app-iniciousuariocomponent',
  standalone: false,
  templateUrl: './iniciousuariocomponent.html',
  styleUrl: './iniciousuariocomponent.css'
})
export class Iniciousuariocomponent implements OnInit {

  /** Usuario con sesión iniciada. */
  usuario: any = null;
  /** Fecha actual. */
  hoy = new Date();

  /** Scrapings del usuario. */
  scrapings: ScrapingDTO[] = [];
  /** Registros del historial del usuario. */
  historiales: HistorialDTO[] = [];

  /** Cantidad de respuestas de las IA en los análisis del usuario. */
  totalResultados = 0;
  /** Cantidad de reportes PDF del usuario. */
  totalReportes = 0;

  /** Indica si todavía se está cargando la información. */
  cargando = true;
  /** Consultas que faltan por terminar (scrapings e historial). */
  consultasPendientes = 2;

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param scrapingService Servicio de scrapings
   * @param historialService Servicio del historial
   * @param resultadoService Servicio de resultados de las IA
   * @param reporteService Servicio de reportes PDF
   * @param toastr Servicio para mostrar notificaciones
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private scrapingService: Scrapingservice,
    private historialService: Historialservice,
    private resultadoService: Resultadoservice,
    private reporteService: Reporteservice,
    private toastr: ToastrService
  ) {}

  /** Carga el usuario y su información al abrir la pantalla. */
  ngOnInit(): void {
    this.obtenerUsuario();
  }

  /** Lee el usuario guardado al iniciar sesión y carga sus datos. */
  obtenerUsuario(): void {

    const datos = localStorage.getItem('usuario');

    if (!datos) {
      this.toastr.error('No se encontró la información del usuario', 'Error');
      this.cargando = false;
      return;
    }

    try {
      this.usuario = JSON.parse(datos);

      if (!this.usuario.id) {
        this.toastr.error('No se encontró el identificador del usuario', 'Error');
        this.cargando = false;
        return;
      }

      this.cargarDatosUsuario(this.usuario.id);

    } catch {
      this.toastr.error('No fue posible cargar el usuario', 'Error');
      this.cargando = false;
    }
  }

  /**
   * Consulta los scrapings y el historial del usuario.
   *
   * @param idUsuario Identificador del usuario
   */
  cargarDatosUsuario(idUsuario: number): void {

    this.scrapingService.buscarPorUsuario(idUsuario).subscribe({
      next: (respuesta) => {
        this.cambios.markForCheck();

        this.scrapings = respuesta || [];

        if (this.scrapings.length > 0) {
          this.cargarResultadosYReportes();
        } else {
          this.finalizarConsulta();
          this.finalizarConsulta();
        }
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No fue posible cargar los scrapings', 'Error');
        this.finalizarConsulta();
        this.finalizarConsulta();
      }
    });

    this.historialService.buscarPorUsuario(idUsuario).subscribe({
      next: (respuesta) => {
        this.cambios.markForCheck();
        this.historiales = respuesta || [];
        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No fue posible cargar el historial', 'Error');
        this.finalizarConsulta();
      }
    });
  }

  /** Cuenta las respuestas de las IA y los reportes de cada scraping del usuario. */
  cargarResultadosYReportes(): void {

    let pendientes = this.scrapings.length * 2;

    this.scrapings.forEach((scraping) => {

      if (!scraping.idScraping) {
        pendientes -= 2;
        this.verificarConsultasTerminadas(pendientes);
        return;
      }

      this.resultadoService.buscarPorScraping(scraping.idScraping).subscribe({
        next: (resultados) => {
          this.cambios.markForCheck();
          this.totalResultados += (resultados || []).length;
          pendientes--;
          this.verificarConsultasTerminadas(pendientes);
        },
        error: () => {
          this.cambios.markForCheck();
          pendientes--;
          this.verificarConsultasTerminadas(pendientes);
        }
      });

      this.reporteService.buscarPorScraping(scraping.idScraping).subscribe({
        next: (reportes) => {
          this.cambios.markForCheck();
          this.totalReportes += (reportes || []).length;
          pendientes--;
          this.verificarConsultasTerminadas(pendientes);
        },
        error: () => {
          this.cambios.markForCheck();
          pendientes--;
          this.verificarConsultasTerminadas(pendientes);
        }
      });
    });
  }

  /**
   * Termina la carga de resultados y reportes cuando ya no quedan consultas.
   *
   * @param pendientes Consultas que faltan
   */
  verificarConsultasTerminadas(pendientes: number): void {

    if (pendientes === 0) {
      this.finalizarConsulta();
    }
  }

  /** Marca una consulta como terminada y quita el estado de carga cuando terminan todas. */
  finalizarConsulta(): void {

    if (this.consultasPendientes > 0) {
      this.consultasPendientes--;
    }

    if (this.consultasPendientes === 0) {
      this.cargando = false;
    }
  }

  /**
   * Obtiene el primer nombre del usuario para el saludo.
   *
   * @returns Primer nombre, o "Usuario" si no tiene
   */
  primerNombre(): string {
    const nombre = this.usuario?.nombre || '';
    return nombre.trim().split(' ')[0] || 'Usuario';
  }

  /**
   * Obtiene los cuatro scrapings más recientes.
   *
   * @returns Scrapings recientes
   */
  recientes(): ScrapingDTO[] {
    return [...this.scrapings]
      .sort((a, b) => {
        return new Date(b.fechaScraping || '').getTime() -
          new Date(a.fechaScraping || '').getTime();
      })
      .slice(0, 4);
  }

  /**
   * Indica si un scraping terminó bien (estado COMPLETADO).
   *
   * @param scraping Scraping a revisar
   * @returns true si el scraping fue exitoso
   */
  esExitoso(scraping: ScrapingDTO): boolean {
    const estado = (scraping.estado || '').toUpperCase();
    return ['COMPLETADO', 'EXITO', 'EXITOSO'].includes(estado);
  }

  /**
   * Cuenta los scrapings exitosos.
   *
   * @returns Cantidad de scrapings exitosos
   */
  totalExitosos(): number {
    return this.scrapings.filter((scraping) => this.esExitoso(scraping)).length;
  }

  /**
   * Cuenta los scrapings fallidos.
   *
   * @returns Cantidad de scrapings fallidos
   */
  totalFallidos(): number {
    return this.scrapings.length - this.totalExitosos();
  }

  /**
   * Calcula el porcentaje de scrapings exitosos.
   *
   * @returns Porcentaje de 0 a 100
   */
  tasaExito(): number {
    const total = this.scrapings.length;

    return total > 0
      ? Math.round((this.totalExitosos() / total) * 100)
      : 0;
  }
}
