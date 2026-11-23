import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from '@relynn/ngx-toastr';
import { Authservice } from '../../services/authservice';

/** Pantalla para crear una cuenta nueva. */
@Component({
  selector: 'app-registrocomponent',
  standalone: false,
  templateUrl: './registrocomponent.html',
  styleUrl: './registrocomponent.css'
})
export class Registrocomponent {

  /** Nombre del usuario. */
  nombre = '';
  /** Apellido del usuario. */
  apellido = '';
  /** Correo del usuario, que también es su nombre de usuario. */
  correo = '';
  /** Contraseña del usuario. */
  password = '';

  /** Indica si la contraseña se muestra en texto visible. */
  mostrarPassword = false;
  /** Indica si se está esperando la respuesta del backend. */
  cargando = false;

  /**
   * Inyecta los servicios que usa la pantalla.
   *
   * @param cambios Detector de cambios para actualizar la vista al llegar las respuestas del backend
   * @param authservice Servicio de autenticación
   * @param toastr Servicio para mostrar notificaciones
   * @param router Router para navegar entre páginas
   */
  constructor(
    private cambios: ChangeDetectorRef,
    private authservice: Authservice,
    private toastr: ToastrService,
    private router: Router
  ) {}

  /**
   * Valida los campos (todos obligatorios, correo válido y contraseña de mínimo 8 caracteres)
   * y registra la cuenta en el backend.
   */
  registrar(): void {

    if (!this.nombre.trim() || !this.apellido.trim() ||
      !this.correo.trim() || !this.password.trim()) {

      this.toastr.warning('Completa todos los campos', 'Campos obligatorios');
      return;
    }

    const expresionCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!expresionCorreo.test(this.correo)) {
      this.toastr.warning('Ingresa un correo electrónico válido', 'Correo inválido');
      return;
    }

    if (this.password.length < 8) {
      this.toastr.warning('La contraseña debe tener mínimo 8 caracteres', 'Contraseña inválida');
      return;
    }

    this.cargando = true;

    this.authservice.registrar({
      username: this.correo.trim(),
      nombre: this.nombre.trim(),
      apellido: this.apellido.trim(),
      password: this.password
    }).subscribe({
      next: (respuesta) => {
        this.cambios.markForCheck();

        this.cargando = false;

        if (respuesta.estado === 'EXITO') {
          this.toastr.success('Tu cuenta fue creada correctamente', 'Registro exitoso');
          this.router.navigate(['/login']);
        } else {
          this.toastr.error(respuesta.mensaje, 'Error en el registro');
        }
      },
      error: (error) => {
        this.cambios.markForCheck();

        this.cargando = false;

        this.toastr.error(
          error.error?.mensaje || 'No fue posible crear tu cuenta',
          'Error'
        );
      }
    });
  }
}
