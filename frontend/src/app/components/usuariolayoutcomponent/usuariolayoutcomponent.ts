import { Component, ViewEncapsulation, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { UsuarioDatosService } from './usuario-datos.service';

/*
 * Contenedor del apartado USUARIO: menú lateral, barra superior,
 * notificaciones y confirmación de cierre de sesión.
 * Sus estilos (encapsulation None) están todos bajo la clase .zona-usuario
 * para que las pantallas hijas los compartan sin afectar el resto del sistema.
 */
@Component({
  selector: 'app-usuariolayoutcomponent',
  standalone: false,
  styleUrls: ['./usuario-compartido.css', './usuariolayoutcomponent.css'],
  templateUrl: './usuariolayoutcomponent.html',
  encapsulation: ViewEncapsulation.None,
})
export class Usuariolayoutcomponent {
  readonly datos = inject(UsuarioDatosService);
  private readonly router = inject(Router);

  menuAbierto = false;
  panelAvisosAbierto = false;
  confirmarSalida = false;

  readonly opciones = [
    { ruta: 'inicio', icono: 'fa-house', texto: 'Inicio' },
    { ruta: 'scraping', icono: 'fa-bolt', texto: 'Nuevo scraping' },
    { ruta: 'historial', icono: 'fa-clock-rotate-left', texto: 'Historial' },
    { ruta: 'reportes', icono: 'fa-file-pdf', texto: 'Reportes PDF' },
    { ruta: 'perfil', icono: 'fa-user', texto: 'Mi perfil' },
  ];

  constructor() {
    // Cierra el menú móvil al navegar.
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        this.menuAbierto = false;
        this.panelAvisosAbierto = false;
      }
    });
  }

  get iniciales(): string {
    return this.datos.perfil().nombreCompleto
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map(p => p[0].toUpperCase())
      .join('');
  }

  alternarAvisos(): void {
    this.panelAvisosAbierto = !this.panelAvisosAbierto;
    if (this.panelAvisosAbierto) {
      // Se marcan como leídos al abrir el panel.
      setTimeout(() => this.datos.marcarAvisosLeidos(), 1500);
    }
  }

  iconoAviso(tipo: string): string {
    return { exito: 'fa-circle-check', error: 'fa-circle-xmark', correo: 'fa-envelope-circle-check', info: 'fa-circle-info' }[tipo] ?? 'fa-bell';
  }

  cerrarSesion(): void {
    // Pendiente: limpiar la sesión real cuando exista autenticación.
    this.confirmarSalida = false;
    this.router.navigate(['/inicio']);
  }
}
