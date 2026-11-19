import {Component, inject} from '@angular/core';
import {Cuenta, Rol, SesionService} from '../../services/sesion.service';

@Component({
  selector: 'app-admingestionusuarioscomponent',
  standalone: false,
  styleUrl: './gestionusuarioscomponent.css',
  templateUrl: './gestionusuarioscomponent.html',
})
export class Gestionusuarioscomponent {

  sesion = inject(SesionService);

  busqueda = '';
  mensaje = '';
  error = false;

  get lista(): Cuenta[] {
    const texto = this.busqueda.trim().toLowerCase();
    if (!texto) return this.sesion.cuentas;

    return this.sesion.cuentas.filter(c =>
      c.nombreCompleto.toLowerCase().includes(texto)
      || c.correo.toLowerCase().includes(texto)
      || c.nombreUsuario.toLowerCase().includes(texto));
  }

  esYo(cuenta: Cuenta): boolean {
    return cuenta.correo === this.sesion.usuarioActual()?.correo;
  }

  cambiarRol(cuenta: Cuenta, rol: string) {
    if (rol !== 'admin' && rol !== 'usuario') return;
    if (cuenta.rol === rol) return;

    if (this.esYo(cuenta)) {
      this.mostrar('No puedes cambiar tu propio rol.', true);
      return;
    }
    // que siempre quede por lo menos un admin
    if (rol === 'usuario' && this.sesion.cuentas.filter(c => c.rol === 'admin').length === 1) {
      this.mostrar('Tiene que quedar al menos un administrador.', true);
      return;
    }

    this.sesion.cambiarRol(cuenta.correo, rol as Rol);
    this.mostrar(cuenta.nombreCompleto + ' ahora es ' + (rol === 'admin' ? 'administrador' : 'usuario') + '.', false);
  }

  // Descarga la tabla
  descargarReporte() {
    let csv = 'Nombre completo;Correo;Usuario;Rol;Fecha de nacimiento;Creación de la cuenta;Lugar\n';
    for (const c of this.lista) {
      csv += [c.nombreCompleto, c.correo, c.nombreUsuario, c.rol, c.fechaNacimiento,
        c.fechaCreacion.substring(0, 10), c.ciudad].join(';') + '\n';
    }

    // tilde

    const archivo = new Blob(['﻿' + csv], {type: 'text/csv;charset=utf-8'});
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(archivo);
    enlace.download = 'reporte-usuarios.csv';
    enlace.click();
    setTimeout(() => URL.revokeObjectURL(enlace.href), 1000);

    this.mostrar('Se descargó el reporte con ' + this.lista.length + ' usuarios.', false);
  }

  private mostrar(texto: string, esError: boolean) {
    this.mensaje = texto;
    this.error = esError;
  }
}
