import { ChangeDetectorRef, Component, computed, OnInit, signal } from '@angular/core';
import { ToastrService } from '@relynn/ngx-toastr';
import { Scrapingservice, ScrapingDTO } from '../../services/scrapingservice';
import { Reporteservice, ReporteDTO } from '../../services/reporteservice';

/** Análisis que se muestra en el historial, con su reporte PDF si tiene. */
interface HistorialUsuario {
  id: number;
  url: string;
  fecha: string;
  estado: 'exitoso' | 'fallido';
  reporte: ReporteDTO | null;
}

/** Pantalla con el historial de análisis del usuario, con búsqueda por URL y filtro por estado. */
@Component({
  selector: 'app-historialcomponent',
  standalone: false,
  styleUrl: './historialcomponent.css',
  templateUrl: './historialcomponent.html'
})
export class Historialcomponent implements OnInit {

  /** Análisis del usuario. */
  historial = signal<HistorialUsuario[]>([]);

  /** Texto para buscar por URL. */
  busqueda = signal('');
  /** Filtro de estado seleccionado. */
  filtro = signal<'todos' | 'exitoso' | 'fallido'>('todos');

  /** Opciones del filtro de estado. */
  filtros: { valor: 'todos' | 'exitoso' | 'fallido'; texto: string }[] = [
    { valor: 'todos', texto: 'Todos' },
    { valor: 'exitoso', texto: 'Exitosos' },
    { valor: 'fallido', texto: 'Fallidos' }
  ];

  /** Análisis que cumplen la búsqueda y el filtro seleccionados. */
  lista = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();

    return this.historial().filter(a =>
      (this.filtro() === 'todos' || a.estado === this.filtro()) &&
      (!texto || a.url.toLowerCase().includes(texto))
    );
  });

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

  /** Carga el historial al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarHistorial();
  }

  /**
   * Consulta los scrapings del usuario con sesión iniciada y el reporte de cada uno,
   * y los ordena del más reciente al más antiguo.
   */
  cargarHistorial(): void {
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
          this.historial.set([]);
          return;
        }

        const registros: HistorialUsuario[] = [];
        let consultasPendientes = scrapings.length;

        scrapings.forEach((scraping) => {

          const registro: HistorialUsuario = {
            id: scraping.idScraping || 0,
            url: scraping.url || 'URL no disponible',
            fecha: scraping.fechaScraping || '',
            estado: this.normalizarEstado(scraping.estado),
            reporte: null
          };

          if (!scraping.idScraping) {
            registros.push(registro);
            consultasPendientes--;

            if (consultasPendientes === 0) {
              this.historial.set(registros);
            }

            return;
          }

          this.reporteService.buscarPorScraping(scraping.idScraping).subscribe({

            next: (reportes: ReporteDTO[]) => {

              this.cambios.markForCheck();

              if (reportes.length > 0) {
                registro.reporte = reportes[0];
              }

              registros.push(registro);
              consultasPendientes--;

              if (consultasPendientes === 0) {
                this.historial.set(
                  registros.sort((a, b) =>
                    new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
                  )
                );
              }
            },

            error: () => {

              this.cambios.markForCheck();
              registros.push(registro);
              consultasPendientes--;

              if (consultasPendientes === 0) {
                this.historial.set(registros);
              }
            }
          });
        });
      },

      error: () => {

        this.cambios.markForCheck();
        this.toastr.error('No fue posible cargar tu historial', 'Error');
      }
    });
  }

  /**
   * Convierte el estado que envía el backend en exitoso o fallido.
   *
   * @param estado Estado del scraping (por ejemplo COMPLETADO o ERROR)
   * @returns exitoso o fallido
   */
  normalizarEstado(estado?: string): 'exitoso' | 'fallido' {
    const valor = (estado || '').toUpperCase();

    if (
      valor === 'EXITO' ||
      valor === 'EXITOSO' ||
      valor === 'COMPLETADO' ||
      valor === 'COMPLETADA' ||
      valor === 'SUCCESS'
    ) {
      return 'exitoso';
    }

    return 'fallido';
  }

  /**
   * Descarga el reporte PDF de un análisis y lo guarda en el equipo del usuario.
   *
   * @param a Análisis del historial
   */
  descargarReporte(a: HistorialUsuario): void {

    if (!a.reporte?.idReporte) {
      this.toastr.warning('Este análisis no tiene un reporte disponible', 'Aviso');
      return;
    }

    this.reporteService.descargarPDF(a.reporte.idReporte).subscribe({

      next: (archivo: Blob) => {

        this.cambios.markForCheck();
        const urlArchivo = window.URL.createObjectURL(archivo);
        const enlace = document.createElement('a');

        enlace.href = urlArchivo;
        enlace.download = a.reporte?.nombreArchivo || 'reporte.pdf';

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
