import { Component, inject } from '@angular/core';
import { UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

/*
 * Perfil del usuario: consultar y editar sus propios datos y cambiar la contraseña.
 * El rol NO es editable. Los cambios solo se guardan en memoria hasta conectar el backend.
 */
@Component({
  selector: 'app-usuarioperfilcomponent',
  standalone: false,
  styleUrl: './usuarioperfilcomponent.css',
  templateUrl: './usuarioperfilcomponent.html',
})
export class Usuarioperfilcomponent {
  readonly datos = inject(UsuarioDatosService);

  editando = false;
  formulario = { nombreCompleto: '', correo: '', nombreUsuario: '' };
  errorDatos = '';

  clave = { actual: '', nueva: '', confirmar: '' };
  errorClave = '';
  fuerza = 0;
  textoFuerza = '';
  verClaves = false;

  get iniciales(): string {
    return this.datos.perfil().nombreCompleto.split(' ').filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('');
  }

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
      correo: this.formulario.correo.trim(),
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
    this.datos.actualizarPerfil(f);
    this.editando = false;
    this.datos.notificar('exito', 'Datos actualizados', 'Tu información personal se guardó correctamente.');
  }

  // Misma lógica de fuerza que el formulario de crear cuenta.
  verificarFuerza(valor: string): void {
    let score = 0;
    if (valor.length >= 6) score++;
    if (valor.length >= 10) score++;
    if (/[A-Z]/.test(valor) && /[0-9]/.test(valor)) score++;
    if (/[^A-Za-z0-9]/.test(valor)) score++;
    this.fuerza = valor ? score : 0;
    if (!valor) this.textoFuerza = '';
    else if (score <= 1) this.textoFuerza = 'Contraseña muy débil';
    else if (score === 2) this.textoFuerza = 'Contraseña moderada';
    else if (score === 3) this.textoFuerza = 'Buena contraseña';
    else this.textoFuerza = 'Contraseña excelente';
  }

  cambiarClave(): void {
    const c = this.clave;
    if (!c.actual || !c.nueva || !c.confirmar) {
      this.errorClave = 'Completa los tres campos.';
    } else if (c.nueva.length < 8) {
      this.errorClave = 'La nueva contraseña debe tener al menos 8 caracteres.';
    } else if (c.nueva !== c.confirmar) {
      this.errorClave = 'Las contraseñas no coinciden.';
    } else if (c.nueva === c.actual) {
      this.errorClave = 'La nueva contraseña debe ser diferente a la actual.';
    } else {
      this.errorClave = '';
      this.clave = { actual: '', nueva: '', confirmar: '' };
      this.verificarFuerza('');
      this.datos.notificar('exito', 'Contraseña actualizada', 'Tu contraseña se cambió correctamente.');
    }
  }
}
