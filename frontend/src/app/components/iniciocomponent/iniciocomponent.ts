import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SesionService } from '../../services/sesion.service';

@Component({
  selector: 'app-iniciocomponent',
  standalone: false,
  styleUrl: './iniciocomponent.css',
  templateUrl: './iniciocomponent.html',
})
export class Iniciocomponent {

  private sesion = inject(SesionService);
  private router = inject(Router);

  correo = '';
  clave = '';
  verClave = false;
  error = '';
  aviso = '';

  iniciarSesion() {
    this.aviso = '';

    if (!this.correo.trim() || !this.clave) {
      this.error = 'Escribe tu correo y tu contraseña.';
      return;
    }

    const cuenta = this.sesion.iniciarSesion(this.correo, this.clave);
    if (!cuenta) {
      this.error = 'Correo o contraseña incorrectos.';
      return;
    }

    this.error = '';
    if (cuenta.rol === 'admin') {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/usuario']);
    }
  }

  olvideClave() {
    this.error = '';
    this.aviso = 'Por ahora la contraseña la restablece un administrador. Escríbele para que te ayude.';
  }
}
