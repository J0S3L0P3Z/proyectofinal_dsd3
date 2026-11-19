import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';
import {SesionService} from '../../services/sesion.service';
import {calcularFuerza} from '../../utils/fuerza-clave';

@Component({
  selector: 'app-crearcuentacomponent',
  standalone: false,
  styleUrl: './crearcuentacomponent.css',
  templateUrl: './crearcuentacomponent.html',
})
export class Crearcuentacomponent {

  private sesion = inject(SesionService);
  private router = inject(Router);

  nombreCompleto = '';
  nombreUsuario = '';
  correo = '';
  fechaNacimiento = '';
  clave = '';
  confirmarClave = '';

  fuerza: number = 0;
  textoFuerza: string = '';
  error = '';
  exito = '';

  // fecha máxima del calendario (no se puede nacer en el futuro)
  hoy = new Date().toISOString().substring(0, 10);

  verificarFuerza(valor: string) {
    const resultado = calcularFuerza(valor);
    this.fuerza = resultado.puntos;
    this.textoFuerza = resultado.texto;
  }

  crearCuenta() {
    this.error = this.validar();
    if (this.error) return;

    const mensaje = this.sesion.registrar({
      nombreCompleto: this.nombreCompleto.trim(),
      nombreUsuario: this.nombreUsuario.trim(),
      correo: this.correo.trim().toLowerCase(),
      clave: this.clave,
      rol: 'usuario',
      fechaNacimiento: this.fechaNacimiento,
      fechaCreacion: new Date().toISOString(),
      ciudad: 'Sin registrar',
    });

    if (mensaje) {
      this.error = mensaje;
      return;
    }

    // Si se creó bien, se inicia sesión de una vez
    this.exito = 'Cuenta creada. Entrando...';
    this.sesion.iniciarSesion(this.correo, this.clave);
    setTimeout(() => this.router.navigate(['/usuario']), 1200);
  }

  private validar(): string {
    if (!this.nombreCompleto.trim() || !this.nombreUsuario.trim() || !this.correo.trim()
      || !this.fechaNacimiento || !this.clave || !this.confirmarClave) {
      return 'Llena todos los campos.';
    }
    if (/\s/.test(this.nombreUsuario.trim())) {
      return 'El nombre de usuario no puede tener espacios.';
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.correo.trim())) {
      return 'El correo no es válido.';
    }
    if (this.calcularEdad(this.fechaNacimiento) < 14) {
      return 'Debes tener al menos 14 años para crear una cuenta.';
    }
    if (this.clave.length < 8) {
      return 'La contraseña debe tener mínimo 8 caracteres.';
    }
    if (this.fuerza < 2) {
      return 'La contraseña es muy débil, combina mayúsculas, números o símbolos.';
    }
    if (this.clave !== this.confirmarClave) {
      return 'Las contraseñas no coinciden.';
    }
    return '';
  }

  private calcularEdad(fecha: string): number {
    const nacimiento = new Date(fecha);
    const hoy = new Date();
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const mes = hoy.getMonth() - nacimiento.getMonth();
    if (mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())) {
      edad--;
    }
    return edad;
  }
}
