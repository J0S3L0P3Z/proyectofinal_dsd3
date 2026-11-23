import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

/**
 * Componente raíz. Muestra la barra de navegación según el rol del usuario, la página actual y el pie de página.
 */
@Component({
  selector: 'app-root',
  standalone: false,
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {

  /** Rol del usuario con sesión iniciada (ADMIN, USER o vacío si no hay sesión). */
  rolUsuario = '';

  /**
   * Lee el rol al iniciar y lo vuelve a leer cada vez que termina una navegación,
   * para cambiar la barra de navegación al iniciar o cerrar sesión.
   *
   * @param router Router para navegar entre páginas
   */
  constructor(private router: Router) {
    this.actualizarRol();

    this.router.events.subscribe(evento => {
      if (evento instanceof NavigationEnd) {
        this.actualizarRol();
      }
    });
  }
  /** Lee el token y el usuario guardados en el navegador y actualiza el rol. */
  actualizarRol(): void {
    const token = localStorage.getItem('token');
    const usuarioGuardado = localStorage.getItem('usuario');

    console.log('Token:', token);
    console.log('Usuario guardado:', usuarioGuardado);

    if (token && usuarioGuardado) {
      try {
        const usuario = JSON.parse(usuarioGuardado);

        console.log('Objeto usuario:', usuario);
        console.log('Rol recibido:', usuario.role);

        this.rolUsuario = (usuario.role || '').toUpperCase();

        console.log('Rol asignado:', this.rolUsuario);

      } catch {
        this.rolUsuario = '';
      }
    } else {
      this.rolUsuario = '';
    }
  }

}
