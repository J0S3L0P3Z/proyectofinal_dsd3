import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ToastrService } from '@relynn/ngx-toastr';
import { Authservice } from '../../services/authservice';

/**
 * Pantalla para recuperar la contraseña en dos pasos: primero se envía un código al correo
 * y después se cambia la contraseña con ese código.
 */
@Component({
  selector: 'app-recuperacionpassword',
  standalone: false,
  templateUrl: './recuperacionpassword.html',
  styleUrl: './recuperacionpassword.css'
})
export class Recuperacionpassword {

  /** Paso actual del proceso: 1 para pedir el código, 2 para cambiar la contraseña. */
  paso = 1;

  /** Correo de la cuenta. */
  correo = '';
  /** Código de 6 dígitos recibido por correo. */
  codigo = '';
  /** Nueva contraseña. */
  nuevaPassword = '';

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

  /** Valida el correo y pide al backend que envíe el código de recuperación. */
  solicitarCodigo(): void {

    if (!this.correo.trim()) {
      this.toastr.warning('Ingresa tu correo electrónico', 'Campo obligatorio');
      return;
    }

    const expresionCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!expresionCorreo.test(this.correo.trim())) {
      this.toastr.warning('Ingresa un correo válido', 'Correo inválido');
      return;
    }

    this.cargando = true;

    this.authservice.solicitarRecuperacion({
      correo: this.correo.trim()
    }).pipe(
      finalize(() => {
        this.cargando = false;
      })
    ).subscribe({
      next: (respuesta) => {
        this.cambios.markForCheck();

        if (respuesta.estado === 'EXITO') {
          this.paso = 2;
          this.toastr.success(
            'Revisa tu correo electrónico',
            'Código enviado'
          );
        } else {
          this.toastr.error(respuesta.mensaje, 'Error');
        }
      },
      error: (error) => {
        this.cambios.markForCheck();

        this.toastr.error(
          error.error?.mensaje || 'No fue posible enviar el código',
          'Error'
        );
      }
    });
  }

  /**
   * Valida el código (6 dígitos) y la nueva contraseña (mínimo 8 caracteres)
   * y cambia la contraseña en el backend.
   */
  cambiarPassword(): void {

    if (!this.codigo.trim() || !this.nuevaPassword.trim()) {
      this.toastr.warning('Completa todos los campos', 'Campos obligatorios');
      return;
    }

    if (!/^\d{6}$/.test(this.codigo.trim())) {
      this.toastr.warning(
        'El código debe tener 6 dígitos',
        'Código inválido'
      );
      return;
    }

    if (this.nuevaPassword.length < 8) {
      this.toastr.warning(
        'La contraseña debe tener mínimo 8 caracteres',
        'Contraseña inválida'
      );
      return;
    }

    this.cargando = true;

    this.authservice.confirmarRecuperacion({
      correo: this.correo.trim(),
      codigo: this.codigo.trim(),
      nuevaPassword: this.nuevaPassword
    }).pipe(
      finalize(() => {
        this.cargando = false;
      })
    ).subscribe({
      next: (respuesta) => {
        this.cambios.markForCheck();

        if (respuesta.estado === 'EXITO') {
          this.toastr.success(
            'Tu contraseña fue actualizada',
            'Recuperación exitosa'
          );

          this.router.navigate(['/login']);
        } else {
          this.toastr.error(respuesta.mensaje, 'Error');
        }
      },
      error: (error) => {
        this.cambios.markForCheck();

        this.toastr.error(
          error.error?.mensaje || 'No fue posible cambiar la contraseña',
          'Error'
        );
      }
    });
  }
}
