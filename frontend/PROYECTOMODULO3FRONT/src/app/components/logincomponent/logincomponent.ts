import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from '@relynn/ngx-toastr';
import { Authservice } from '../../services/authservice';

/** Pantalla de inicio de sesión. */
@Component({
  selector: 'app-logincomponent',
  standalone: false,
  templateUrl: './logincomponent.html',
  styleUrls: ['./logincomponent.css']
})
export class Logincomponent {

  /** Correo escrito por el usuario. */
  username: string = '';
  /** Contraseña escrita por el usuario. */
  password: string = '';

  /** Indica si la contraseña se muestra en texto visible. */
  mostrarPassword: boolean = false;
  /** Indica si se está esperando la respuesta del backend. */
  cargando: boolean = false;

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param authService Servicio de autenticación
   * @param toastr Servicio para mostrar notificaciones
   * @param router Router para navegar entre páginas
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private authService: Authservice,
    private toastr: ToastrService,
    private router: Router
  ) {}

  /**
   * Valida los campos e inicia sesión. Si es correcto, guarda el token y el usuario
   * y lleva al inicio del administrador o del usuario según su rol.
   */
  iniciarSesion(): void {

    if (!this.username.trim() || !this.password.trim()) {
      this.toastr.warning('Debe ingresar el correo y la contraseña.');
      return;
    }

    this.cargando = true;

    this.authService.iniciarSesion({
      username: this.username.trim(),
      password: this.password
    }).subscribe({

      next: (respuesta) => {

        this.cambios.markForCheck();

        if (respuesta.estado === 'EXITO' && respuesta.token && respuesta.usuario) {

          localStorage.setItem('token', respuesta.token);
          localStorage.setItem('usuario', JSON.stringify(respuesta.usuario));

          this.toastr.success('¡Bienvenido a ScrapingDoo!');

          if (respuesta.usuario.role === 'ADMIN') {
            this.router.navigate(['/inicioadministrador']);
          } else {
            this.router.navigate(['/iniciousuario']);
          }

        } else {
          this.toastr.error(respuesta.mensaje || 'No fue posible iniciar sesión.');
        }

        this.cargando = false;
      },

      error: (error) => {

        this.cambios.markForCheck();

        this.toastr.error(
          error.error?.mensaje || 'Correo o contraseña incorrectos.'
        );

        this.cargando = false;
      }
    });
  }

  /** Muestra u oculta la contraseña. */
  cambiarVisibilidadPassword(): void {
    this.mostrarPassword = !this.mostrarPassword;
  }
}
