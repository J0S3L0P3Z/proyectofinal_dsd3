import { Component } from '@angular/core';
import { Router } from '@angular/router';

/** Barra de navegación del usuario. */
@Component({
  selector: 'app-navbarusuariocomponent',
  standalone: false,
  templateUrl: './navbarusuariocomponent.html',
  styleUrl: './navbarusuariocomponent.css'
})
export class Navbarusuariocomponent {

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
