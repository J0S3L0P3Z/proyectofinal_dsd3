import { ChangeDetectorRef, Component, OnInit, computed, signal } from '@angular/core';
import { ToastrService } from '@relynn/ngx-toastr';
import { Scrapingservice, ScrapingDTO } from '../../services/scrapingservice';
import { Reporteservice, ReporteDTO } from '../../services/reporteservice';

/** Reporte PDF que se muestra en la lista, con los datos de su análisis. */
interface ReporteUsuario {
  id: number;
  url: string;
  fecha: string;
  reporte: ReporteDTO;
}

/** Pantalla con los reportes PDF del usuario. */
@Component({
  selector: 'app-reportecomponent',
  standalone: false,
  styleUrl: './reportecomponent.css',
  templateUrl: './reportecomponent.html'
})
export class Reportecomponent implements OnInit {

  /** Reportes del usuario. */
  reportes = signal<ReporteUsuario[]>([]);
  /** Reportes que se muestran en la lista. */
  conReporte = computed(() => this.reportes());

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

  /** Carga los reportes al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarReportes();
  }

  /** Consulta los scrapings del usuario con sesión iniciada y los reportes de cada uno. */
  cargarReportes(): void {
    const datos = localStorage.getItem('usuario');

    if (!datos) {
      this.toastr.error('No se encontró el usuario', 'Error');
      return;
    }

    let usuario: any;

    try {
      usuario = JSON.parse(datos);
    } catch {
      this.toastr.error('No fue posible cargar los datos del usuario', 'Error');
      return;
    }

    if (!usuario.id) {
      this.toastr.error('El usuario no tiene un identificador válido', 'Error');
      return;
    }

    this.scrapingService.buscarPorUsuario(usuario.id).subscribe({

      next: (scrapings: ScrapingDTO[]) => {

        this.cambios.markForCheck();

        if (scrapings.length === 0) {
          this.reportes.set([]);
          return;
        }

        let consultasPendientes = scrapings.length;
        const reportesEncontrados: ReporteUsuario[] = [];

        scrapings.forEach((scraping) => {

          if (!scraping.idScraping) {
            consultasPendientes--;

            if (consultasPendientes === 0) {
              this.reportes.set(reportesEncontrados);
            }

            return;
          }

          this.reporteService.buscarPorScraping(scraping.idScraping).subscribe({

            next: (respuesta: ReporteDTO[]) => {

              this.cambios.markForCheck();

              respuesta.forEach((reporte) => {
                reportesEncontrados.push({
                  id: scraping.idScraping!,
                  url: scraping.url || 'URL no disponible',
                  fecha: scraping.fechaScraping || '',
                  reporte: reporte
                });
              });

              consultasPendientes--;

              if (consultasPendientes === 0) {
                this.reportes.set(reportesEncontrados);
              }
            },

            error: () => {

              this.cambios.markForCheck();
              consultasPendientes--;

              if (consultasPendientes === 0) {
                this.reportes.set(reportesEncontrados);
              }
            }
          });
        });
      },

      error: () => {

        this.cambios.markForCheck();
        this.toastr.error('No fue posible consultar tus análisis', 'Error');
      }
    });
  }

  /**
   * Descarga el reporte PDF seleccionado y lo guarda en el equipo del usuario.
   *
   * @param a Reporte a descargar
   */
  descargarReporte(a: ReporteUsuario): void {

    if (!a.reporte.idReporte) {
      this.toastr.warning('No se encontró el identificador del reporte', 'Aviso');
      return;
    }

    this.reporteService.descargarPDF(a.reporte.idReporte).subscribe({

      next: (archivo: Blob) => {

        this.cambios.markForCheck();

        const urlArchivo = window.URL.createObjectURL(archivo);
        const enlace = document.createElement('a');

        enlace.href = urlArchivo;
        enlace.download = a.reporte.nombreArchivo || 'reporte.pdf';

        enlace.click();

        window.URL.revokeObjectURL(urlArchivo);

        this.toastr.success('El reporte se descargó correctamente', 'Descarga exitosa');
      },

      error: () => {

        this.cambios.markForCheck();
        this.toastr.error('No fue posible descargar el reporte', 'Error');
      }
    });
  }
}
