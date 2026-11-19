import {Component, inject} from '@angular/core';
import {Cuenta, SesionService} from '../../services/sesion.service';
import {AdminDatosService} from '../../services/admin-datos.service';
import {calcularFuerza} from '../../utils/fuerza-clave';


@Component({
  selector: 'app-adminperfilcomponent',
  standalone: false,
  styleUrl: './adminperfilcomponent.css',
  templateUrl: './adminperfilcomponent.html',
})
export class Adminperfilcomponent {

  private sesion = inject(SesionService);
  private adminDatos = inject(AdminDatosService);

  editando = false;
  formulario = {nombreCompleto: '', correo: '', nombreUsuario: ''};
  errorDatos = '';
  exitoDatos = '';

  clave = {actual: '', nueva: '', confirmar: ''};
  errorClave = '';
  exitoClave = '';
  fuerza = 0;
  textoFuerza = '';
  verClaves = false;

  // Si por algo no hay sesión (por ejemplo en las pruebas) se muestra vacío
  get cuenta(): Cuenta {
    return this.sesion.usuarioActual() ?? {
      nombreCompleto: 'Administrador', nombreUsuario: 'admin', correo: '', clave: '', rol: 'admin',
      fechaNacimiento: '', fechaCreacion: new Date().toISOString(), ciudad: '',
    };
  }

  get totalScrapings(): number {
    return this.adminDatos.misScrapings().length;
  }

  get exitosos(): number {
    return this.adminDatos.misScrapings().filter(s => s.exitoso).length;
  }

  get fallidos(): number {
    return this.totalScrapings - this.exitosos;
  }

  editar(): void {
    const c = this.cuenta;
    this.formulario = {nombreCompleto: c.nombreCompleto, correo: c.correo, nombreUsuario: c.nombreUsuario};
    this.errorDatos = '';
    this.exitoDatos = '';
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
    if (f.correo !== this.cuenta.correo && this.sesion.buscarPorCorreo(f.correo)) {
      this.errorDatos = 'Ese correo ya lo usa otra cuenta.';
      return;
    }

    this.sesion.actualizarCuenta(this.cuenta.correo, f);
    this.editando = false;
    this.exitoDatos = 'Tus datos se guardaron correctamente.';
  }

  verificarFuerza(valor: string): void {
    const resultado = calcularFuerza(valor);
    this.fuerza = resultado.puntos;
    this.textoFuerza = resultado.texto;
  }

  cambiarClave(): void {
    const c = this.clave;
    this.exitoClave = '';

    if (!c.actual || !c.nueva || !c.confirmar) {
      this.errorClave = 'Completa los tres campos.';
    } else if (c.nueva.length < 8) {
      this.errorClave = 'La nueva contraseña debe tener al menos 8 caracteres.';
    } else if (c.nueva !== c.confirmar) {
      this.errorClave = 'Las contraseñas no coinciden.';
    } else if (c.nueva === c.actual) {
      this.errorClave = 'La nueva contraseña debe ser diferente a la actual.';
    } else if (c.actual !== this.cuenta.clave) {
      this.errorClave = 'La contraseña actual no es correcta.';
    } else {
      this.sesion.actualizarCuenta(this.cuenta.correo, {clave: c.nueva});
      this.errorClave = '';
      this.exitoClave = 'Tu contraseña se cambió correctamente.';
      this.clave = {actual: '', nueva: '', confirmar: ''};
      this.verificarFuerza('');
    }
  }
}
