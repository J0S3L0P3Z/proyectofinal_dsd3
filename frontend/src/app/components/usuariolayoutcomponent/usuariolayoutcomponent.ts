import { Component, ViewEncapsulation, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { UsuarioDatosService } from '../../services/usuario-datos.service';
import { SesionService } from '../../services/sesion.service';

// Contenedor de todas las páginas del usuario (menú lateral, campana y cerrar sesión).
// Va con encapsulation None para que las páginas hijas usen los mismos estilos (todos van dentro de .zona-usuario)
@Component({
  selector: 'app-usuariolayoutcomponent',
  standalone: false,
  styleUrl: './usuariolayoutcomponent.css',
  templateUrl: './usuariolayoutcomponent.html',
  encapsulation: ViewEncapsulation.None,
})
export class Usuariolayoutcomponent {
  datos = inject(UsuarioDatosService);
  private sesion = inject(SesionService);
  private router = inject(Router);

  menuAbierto = false;
  panelAvisosAbierto = false;
  confirmarSalida = false;

  opciones = [
    { ruta: 'inicio', icono: 'fa-house', texto: 'Inicio' },
    { ruta: 'scraping', icono: 'fa-bolt', texto: 'Nuevo scraping' },
    { ruta: 'historial', icono: 'fa-clock-rotate-left', texto: 'Historial' },
    { ruta: 'reportes', icono: 'fa-file-pdf', texto: 'Reportes PDF' },
    { ruta: 'perfil', icono: 'fa-user', texto: 'Mi perfil' },
  ];

  constructor() {
    const usuario = this.sesion.usuarioActual();
    if (usuario) {
      this.datos.cargarPerfil(usuario);
    }

    // al cambiar de página se cierran el menú del celular y las notificaciones
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        this.menuAbierto = false;
        this.panelAvisosAbierto = false;
      }
    });
  }

  get iniciales(): string {
    const partes = this.datos.perfil().nombreCompleto.split(' ').filter(p => p !== '');
    let iniciales = '';
    for (let i = 0; i < partes.length && i < 2; i++) {
      iniciales += partes[i][0].toUpperCase();
    }
    return iniciales;
  }

  alternarAvisos(): void {
    this.panelAvisosAbierto = !this.panelAvisosAbierto;
    if (this.panelAvisosAbierto) {
      // se marcan como leídas un momento después de abrir el panel
      setTimeout(() => this.datos.marcarAvisosLeidos(), 1500);
    }
  }

  iconoAviso(tipo: string): string {
    switch (tipo) {
      case 'exito': return 'fa-circle-check';
      case 'error': return 'fa-circle-xmark';
      case 'correo': return 'fa-envelope-circle-check';
      case 'info': return 'fa-circle-info';
      default: return 'fa-bell';
    }
  }

  cerrarSesion(): void {
    this.confirmarSalida = false;
    this.sesion.cerrarSesion();
    this.router.navigate(['/inicio']);
  }
}
