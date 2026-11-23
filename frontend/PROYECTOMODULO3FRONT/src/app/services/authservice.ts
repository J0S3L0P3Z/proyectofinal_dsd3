import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/** Dirección del backend desplegado. Todos los servicios la usan para armar sus rutas. */
export const URL_BACK = 'https://gpcueb.org/SCRAPINGDOO!!';

/** Datos de un usuario tal como los devuelve el backend. */
export interface UserDTO {
  id?: number;
  username?: string;
  nombre?: string;
  apellido?: string;
  role?: string;
  fechaRegistro?: string;
  enabled?: boolean;
  activo?: boolean;
}

/** Respuesta del backend en el login, el registro y la recuperación de contraseña. */
export interface AuthResponse {
  estado: string;
  mensaje: string;
  token?: string;
  usuario?: UserDTO;
}

/** Datos que se envían para registrar un usuario nuevo. */
export interface RegistroUsuario {
  username: string;
  nombre: string;
  apellido: string;
  password: string;
}

/** Credenciales que se envían para iniciar sesión. */
export interface LoginDatos {
  username: string;
  password: string;
}

/** Correo al que se envía el código para recuperar la contraseña. */
export interface RecuperarPasswordDatos {
  correo: string;
}

/** Datos para cambiar la contraseña con el código recibido por correo. */
export interface ConfirmarRecuperacionDatos {
  correo: string;
  codigo: string;
  nuevaPassword: string;
}

/**
 * Servicio de autenticación: registro, inicio de sesión y recuperación de contraseña (rutas /auth del backend, que no piden token).
 */
@Injectable({
  providedIn: 'root'
})
export class Authservice {

  /** Ruta base de autenticación en el backend. */
  private url = URL_BACK + '/auth';

  /**
   * Inyecta el cliente HTTP para comunicarse con el backend.
   *
   * @param http Cliente HTTP de Angular
   */
  constructor(private http: HttpClient) {}

  /**
   * Registra un usuario nuevo.
   *
   * @param usuario Datos del usuario a registrar
   * @returns Respuesta del backend con el estado del registro
   */
  registrar(usuario: RegistroUsuario): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.url + '/register', usuario);
  }

  /**
   * Inicia sesión con correo y contraseña.
   *
   * @param datos Credenciales del usuario
   * @returns Respuesta con el token JWT y los datos del usuario
   */
  iniciarSesion(datos: LoginDatos): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.url + '/login', datos);
  }

  /**
   * Pide al backend que envíe un código de recuperación al correo.
   *
   * @param datos Correo del usuario
   * @returns Respuesta del backend con el estado del envío
   */
  solicitarRecuperacion(datos: RecuperarPasswordDatos): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.url + '/recuperar-password', datos);
  }

  /**
   * Cambia la contraseña usando el código recibido por correo.
   *
   * @param datos Correo, código y nueva contraseña
   * @returns Respuesta del backend con el estado del cambio
   */
  confirmarRecuperacion(datos: ConfirmarRecuperacionDatos): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.url + '/confirmar-recuperacion', datos);
  }
}
