<<<<<<< Updated upstream
import { Component , OnInit } from '@angular/core';
=======
import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { UsuarioDatosService } from '../../services/usuario-datos.service';
import { SesionService } from '../../services/sesion.service';
>>>>>>> Stashed changes

@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  styleUrl: './navbarcomponent.css',
  templateUrl: './navbarcomponent.html',
})
export class Navbarcomponent {

  datosUsuario = inject(UsuarioDatosService);
  sesion = inject(SesionService);
  private router = inject(Router);

<<<<<<< Updated upstream
  constructor() { }
=======
  // Qué navbar se muestra: la del usuario, la del admin o la pública (login / crear cuenta)
  seccion = signal<'usuario' | 'admin' | 'publica'>('publica');

  opcionesAdmin = [
    { ruta: 'inicio', icono: 'fa-house', texto: 'Inicio' },
    { ruta: 'scraping', icono: 'fa-magnifying-glass-location', texto: 'Scraping' },
    { ruta: 'historial', icono: 'fa-clock-rotate-left', texto: 'Historial' },
    { ruta: 'estadisticas', icono: 'fa-chart-line', texto: 'Estadísticas' },
    { ruta: 'usuarios', icono: 'fa-users-gear', texto: 'Usuarios' },
  ];
>>>>>>> Stashed changes

  constructor() {
    this.revisarRuta(this.router.url);

    this.router.events.subscribe(evento => {
      if (evento instanceof NavigationEnd) {
        this.revisarRuta(evento.urlAfterRedirects);
      }
    });
  }

  revisarRuta(url: string) {
    if (url.startsWith('/usuario')) {
      this.seccion.set('usuario');
    } else if (url.startsWith('/admin')) {
      this.seccion.set('admin');
    } else {
      this.seccion.set('publica');
    }
  }

  cerrarSesion() {
    this.sesion.cerrarSesion();
    this.router.navigate(['/inicio']);
  }
}
