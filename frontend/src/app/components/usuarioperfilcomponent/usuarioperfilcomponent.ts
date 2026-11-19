import { Component, inject } from '@angular/core';
import { UsuarioDatosService } from '../../services/usuario-datos.service';
import { SesionService } from '../../services/sesion.service';
import { calcularFuerza } from '../../utils/fuerza-clave';

// El rol no se puede editar desde aquí, eso solo lo cambia un admin
@Component({
  selector: 'app-usuarioperfilcomponent',
  standalone: false,
  styleUrl: './usuarioperfilcomponent.css',
  templateUrl: './usuarioperfilcomponent.html',
})
export class Usuarioperfilcomponent {
  datos = inject(UsuarioDatosService);
  private sesion = inject(SesionService);

  editando = false;
  formulario = { nombreCompleto: '', correo: '', nombreUsuario: '' };
  errorDatos = '';

  clave = { actual: '', nueva: '', confirmar: '' };
  errorClave = '';
  fuerza = 0;
  textoFuerza = '';
  verClaves = false;

  editar(): void {
    const p = this.datos.perfil();
    this.formulario = { nombreCompleto: p.nombreCompleto, correo: p.correo, nombreUsuario: p.nombreUsuario };
    this.errorDatos = '';
    this.editando = true;
  }

  cancelarEdicion(): void {
    this.editando = false;
    this.errorDatos = '';
  }

  guardarDatos(): void {
    const f = {
      nombreCompleto: this.formulario.nombreCompleto.trim(),
      correo: this.formulario.correo.trim().toLowerCase(),
      nombreUsuario: this.formulario.nombreUsuario.trim(),
    };
    if (!f.nombreCompleto || !f.correo || !f.nombreUsuario) {
      this.errorDatos = 'Completa todos los campos.';
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.correo)) {
      this.errorDatos = 'Ingresa un correo electrónico válido.';
      return;
    }
    // que el correo nuevo no sea de otra cuenta
    if (f.correo !== this.datos.perfil().correo && this.sesion.buscarPorCorreo(f.correo)) {
      this.errorDatos = 'Ese correo ya lo usa otra cuenta.';
      return;
    }
    this.datos.actualizarPerfil(f);
    this.editando = false;
    this.datos.notificar('exito', 'Datos actualizados', 'Tu información personal se guardó correctamente.');
  }

  verificarFuerza(valor: string): void {
    const resultado = calcularFuerza(valor);
    this.fuerza = resultado.puntos;
    this.textoFuerza = resultado.texto;
  }

  cambiarClave(): void {
    const c = this.clave;
    const cuenta = this.sesion.usuarioActual();

    if (!c.actual || !c.nueva || !c.confirmar) {
      this.errorClave = 'Completa los tres campos.';
    } else if (c.nueva.length < 8) {
      this.errorClave = 'La nueva contraseña debe tener al menos 8 caracteres.';
    } else if (c.nueva !== c.confirmar) {
      this.errorClave = 'Las contraseñas no coinciden.';
    } else if (c.nueva === c.actual) {
      this.errorClave = 'La nueva contraseña debe ser diferente a la actual.';
    } else if (cuenta && cuenta.clave !== c.actual) {
      this.errorClave = 'La contraseña actual no es correcta.';
    } else {
      if (cuenta) {
        this.sesion.actualizarCuenta(cuenta.correo, { clave: c.nueva });
      }
      this.errorClave = '';
      this.clave = { actual: '', nueva: '', confirmar: '' };
      this.verificarFuerza('');
      this.datos.notificar('exito', 'Contraseña actualizada', 'Tu contraseña se cambió correctamente.');
    }
  }
}
