import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ToastrService } from '@relynn/ngx-toastr';

import { Userservice } from '../../services/userservice';
import { Scrapingservice, ScrapingDTO } from '../../services/scrapingservice';
import { Historialservice } from '../../services/historialservice';
import { Resultadoservice } from '../../services/resultadoservice';

/** Pantalla de inicio del administrador con las estadísticas generales del sistema. */
@Component({
  selector: 'app-inicioadministradorcomponent',
  standalone: false,
  templateUrl: './inicioadministradorcomponent.html',
  styleUrl: './inicioadministradorcomponent.css'
})
export class Inicioadministradorcomponent implements OnInit {

  /** Saludo según la hora del día. */
  saludo = '';
  /** Nombre del administrador para el saludo. */
  primerNombre = '';
  /** Fecha actual en texto. */
  fechaHoy = '';

  /** Cantidad total de usuarios. */
  totalUsuarios = 0;
  /** Cantidad de administradores. */
  totalAdministradores = 0;
  /** Cantidad de usuarios con rol USER. */
  totalUsuariosNormales = 0;
  /** Cantidad total de scrapings. */
  totalScrapings = 0;
  /** Cantidad total de respuestas de las IA. */
  totalResultados = 0;
  /** Cantidad total de registros del historial. */
  totalHistoriales = 0;

  /** Los cinco scrapings más recientes. */
  ultimosScrapings: ScrapingDTO[] = [];

  /** Indica si todavía se están cargando las estadísticas. */
  cargando = true;
  /** Consultas al backend que faltan por terminar. */
  consultasPendientes = 6;

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param usuarioService Servicio de usuarios
   * @param scrapingService Servicio de scrapings
   * @param historialService Servicio del historial
   * @param resultadoService Servicio de resultados de las IA
   * @param toastr Servicio para mostrar notificaciones
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private usuarioService: Userservice,
    private scrapingService: Scrapingservice,
    private historialService: Historialservice,
    private resultadoService: Resultadoservice,
    private toastr: ToastrService
  ) {}

  /** Arma la bienvenida y carga las estadísticas al abrir la pantalla. */
  ngOnInit(): void {
    this.configurarBienvenida();
    this.cargarEstadisticas();
  }

  /** Arma el saludo según la hora, la fecha de hoy y el nombre del administrador. */
  configurarBienvenida(): void {

    const hora = new Date().getHours();

    if (hora >= 5 && hora < 12) {
      this.saludo = 'Buenos días';
    } else if (hora >= 12 && hora < 19) {
      this.saludo = 'Buenas tardes';
    } else {
      this.saludo = 'Buenas noches';
    }

    this.fechaHoy = new Date().toLocaleDateString('es-CO', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });

    const datosUsuario = localStorage.getItem('usuario');

    if (datosUsuario) {
      try {
        const usuario = JSON.parse(datosUsuario);
        this.primerNombre = usuario.nombre || usuario.username || 'Administrador';
      } catch {
        this.primerNombre = 'Administrador';
      }
    } else {
      this.primerNombre = 'Administrador';
    }
  }

  /** Hace las seis consultas de estadísticas al backend. */
  cargarEstadisticas(): void {

    this.usuarioService.contar().subscribe({
      next: (cantidad) => {
        this.cambios.markForCheck();
        this.totalUsuarios = cantidad;
        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No se pudo cargar el total de usuarios.');
        this.finalizarConsulta();
      }
    });

    this.usuarioService.contarAdministradores().subscribe({
      next: (cantidad) => {
        this.cambios.markForCheck();
        this.totalAdministradores = cantidad;
        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No se pudo cargar el total de administradores.');
        this.finalizarConsulta();
      }
    });

    this.usuarioService.contarUsuarios().subscribe({
      next: (cantidad) => {
        this.cambios.markForCheck();
        this.totalUsuariosNormales = cantidad;
        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No se pudo cargar el total de usuarios normales.');
        this.finalizarConsulta();
      }
    });

    this.scrapingService.buscarTodos().subscribe({
      next: (datos) => {
        this.cambios.markForCheck();

        const scrapings = datos || [];

        this.totalScrapings = scrapings.length;

        this.ultimosScrapings = [...scrapings]
          .sort((a, b) =>
            new Date(b.fechaScraping || '').getTime() -
            new Date(a.fechaScraping || '').getTime()
          )
          .slice(0, 5);

        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No se pudieron cargar los scrapings.');
        this.finalizarConsulta();
      }
    });

    this.resultadoService.buscarTodos().subscribe({
      next: (datos) => {
        this.cambios.markForCheck();
        this.totalResultados = (datos || []).length;
        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No se pudo cargar el total de resultados.');
        this.finalizarConsulta();
      }
    });

    this.historialService.buscarTodos().subscribe({
      next: (datos) => {
        this.cambios.markForCheck();
        this.totalHistoriales = (datos || []).length;
        this.finalizarConsulta();
      },
      error: () => {
        this.cambios.markForCheck();
        this.toastr.error('No se pudo cargar el total del historial.');
        this.finalizarConsulta();
      }
    });
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
}
