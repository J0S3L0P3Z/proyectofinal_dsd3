import {Injectable, signal} from '@angular/core';

export type Rol = 'admin' | 'usuario';

export interface Cuenta {
  nombreCompleto: string;
  nombreUsuario: string;
  correo: string;
  clave: string;
  rol: Rol;
  fechaNacimiento: string;
  fechaCreacion: string;
  ciudad: string;
}

// Mientras no tengamos el backend las cuentas quedan en el localStorage del navegador
const LLAVE_CUENTAS = 'scrapingdoo_cuentas';
const LLAVE_SESION = 'scrapingdoo_sesion';

@Injectable({providedIn: 'root'})
export class SesionService {

  cuentas: Cuenta[] = [
    {
      nombreCompleto: 'Julio Hernández',
      nombreUsuario: 'xtuliox.x',
      correo: 'admin@scrapingdoo.com',
      clave: 'Admin123*',
      rol: 'admin',
      fechaNacimiento: '2005-03-14',
      fechaCreacion: '2026-08-01T08:00:00',
      ciudad: 'Bogotá, CO'
    },
    {
      nombreCompleto: 'Daniel Malagón',
      nombreUsuario: 'danielmalagon',
      correo: 'daniel.malagon@correo.com',
      clave: 'Usuario123*',
      rol: 'usuario',
      fechaNacimiento: '2005-07-02',
      fechaCreacion: '2026-08-21T10:00:00',
      ciudad: 'Bogotá, CO'
    },
    {
      nombreCompleto: 'Mariana Torres',
      nombreUsuario: 'marianat',
      correo: 'mariana@gmail.com',
      clave: 'Mariana123*',
      rol: 'admin',
      fechaNacimiento: '2004-01-20',
      fechaCreacion: '2026-11-09T18:20:00',
      ciudad: 'Medellín, CO'
    },
    {
      nombreCompleto: 'Carlos Delgado',
      nombreUsuario: 'carlosd',
      correo: 'carlos@gmail.com',
      clave: 'Carlos123*',
      rol: 'usuario',
      fechaNacimiento: '2003-05-11',
      fechaCreacion: '2026-11-08T11:15:00',
      ciudad: 'Cali, CO'
    },
    {
      nombreCompleto: 'Ana Gómez',
      nombreUsuario: 'anagomez',
      correo: 'ana@gmail.com',
      clave: 'Ana12345*',
      rol: 'usuario',
      fechaNacimiento: '2006-09-30',
      fechaCreacion: '2026-11-07T09:45:00',
      ciudad: 'Barranquilla, CO'
    },
    {
      nombreCompleto: 'Diego Rodríguez',
      nombreUsuario: 'diegor',
      correo: 'diego@gmail.com',
      clave: 'Diego123*',
      rol: 'admin',
      fechaNacimiento: '2002-12-05',
      fechaCreacion: '2026-11-06T20:10:00',
      ciudad: 'Bogotá, CO'
    },
  ];

  usuarioActual = signal<Cuenta | null>(null);

  constructor() {
    const guardadas = localStorage.getItem(LLAVE_CUENTAS);
    if (guardadas) {
      this.cuentas = JSON.parse(guardadas);
    }

    const correoSesion = localStorage.getItem(LLAVE_SESION);
    if (correoSesion) {
      this.usuarioActual.set(this.buscarPorCorreo(correoSesion) ?? null);
    }
  }

  iniciarSesion(correo: string, clave: string): Cuenta | null {
    const cuenta = this.buscarPorCorreo(correo);
    if (!cuenta || cuenta.clave !== clave) {
      return null;
    }
    this.usuarioActual.set(cuenta);
    localStorage.setItem(LLAVE_SESION, cuenta.correo);
    return cuenta;
  }

  cerrarSesion() {
    this.usuarioActual.set(null);
    localStorage.removeItem(LLAVE_SESION);
  }

  // Devuelve un mensaje de error, o vacío si se pudo registrar
  registrar(cuenta: Cuenta): string {
    if (this.buscarPorCorreo(cuenta.correo)) {
      return 'Ya existe una cuenta con ese correo.';
    }
    if (this.cuentas.some(c => c.nombreUsuario.toLowerCase() === cuenta.nombreUsuario.toLowerCase())) {
      return 'Ese nombre de usuario ya está en uso.';
    }
    this.cuentas.push(cuenta);
    this.guardar();
    return '';
  }

  cambiarRol(correo: string, rol: Rol) {
    const cuenta = this.buscarPorCorreo(correo);
    if (cuenta) {
      cuenta.rol = rol;
      this.guardar();
    }
  }

  // Se usa desde el perfil del usuario cuando cambia sus datos o la contraseña
  actualizarCuenta(correoAnterior: string, cambios: Partial<Cuenta>) {
    const cuenta = this.buscarPorCorreo(correoAnterior);
    if (!cuenta) return;

    Object.assign(cuenta, cambios);
    this.guardar();

    if (this.usuarioActual()?.correo === correoAnterior) {
      this.usuarioActual.set({...cuenta});
      localStorage.setItem(LLAVE_SESION, cuenta.correo);
    }
  }

  buscarPorCorreo(correo: string): Cuenta | undefined {
    return this.cuentas.find(c => c.correo.toLowerCase() === correo.trim().toLowerCase());
  }

  private guardar() {
    localStorage.setItem(LLAVE_CUENTAS, JSON.stringify(this.cuentas));
  }
}
