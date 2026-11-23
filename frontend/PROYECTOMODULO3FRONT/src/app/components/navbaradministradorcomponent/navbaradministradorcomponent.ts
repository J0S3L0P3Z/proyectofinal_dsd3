import { Component } from '@angular/core';
import { Router } from '@angular/router';

/** Barra de navegación del administrador. */
@Component({
  selector: 'app-navbaradministradorcomponent',
  standalone: false,
  templateUrl: './navbaradministradorcomponent.html',
  styleUrl: './navbaradministradorcomponent.css'
})
export class Navbaradministradorcomponent {

  /**
   * Inyecta el router.
   *
   * @param router Router para navegar entre páginas
   */
  constructor(private router: Router) {}

  /** Borra el token y el usuario guardados y vuelve al login. */
  cerrarSesion(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    this.router.navigate(['/login']);
  }
}
