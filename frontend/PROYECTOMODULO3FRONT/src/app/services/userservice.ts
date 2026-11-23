import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_BACK } from './authservice';

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

/**
 * Servicio para administrar los usuarios (rutas /usuarios del backend, solo para administradores).
 */
@Injectable({
  providedIn: 'root'
})
export class Userservice {

  /** Ruta base de los usuarios en el backend. */
  private url = URL_BACK + '/usuarios';

  /**
   * Inyecta el cliente HTTP para comunicarse con el backend.
   *
   * @param http Cliente HTTP de Angular
   */
  constructor(private http: HttpClient) {}

  /**
   * Arma la cabecera Authorization con el token JWT guardado al iniciar sesión.
   * Si no hay token, devuelve las cabeceras vacías.
   *
   * @returns Cabeceras HTTP con el token, si existe
   */
  private obtenerHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');

    return new HttpHeaders(
      token ? { Authorization: `Bearer ${token}` } : {}
    );
  }

  /**
   * Consulta todos los usuarios.
   *
   * @returns Lista de usuarios
   */
  buscarTodos(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(this.url, {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Consulta un usuario por su identificador.
   *
   * @param id Identificador del usuario
   * @returns Usuario encontrado
   */
  buscarPorId(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(this.url + '/' + id, {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Consulta un usuario por su nombre de usuario (correo).
   *
   * @param username Nombre de usuario
   * @returns Usuario encontrado
   */
  buscarPorUsername(username: string): Observable<UserDTO> {
    return this.http.get<UserDTO>(
      this.url + '/username/' + encodeURIComponent(username),
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Cuenta todos los usuarios registrados.
   *
   * @returns Cantidad de usuarios
   */
  contar(): Observable<number> {
    return this.http.get<number>(this.url + '/contar', {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Cuenta los usuarios con rol ADMIN.
   *
   * @returns Cantidad de administradores
   */
  contarAdministradores(): Observable<number> {
    return this.http.get<number>(this.url + '/contar/administradores', {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Cuenta los usuarios con rol USER.
   *
   * @returns Cantidad de usuarios normales
   */
  contarUsuarios(): Observable<number> {
    return this.http.get<number>(this.url + '/contar/usuarios', {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Actualiza los datos de un usuario.
   *
   * @param id Identificador del usuario
   * @param datos Nuevos datos del usuario
   * @returns Usuario actualizado
   */
  actualizar(id: number, datos: UserDTO): Observable<UserDTO> {
    return this.http.put<UserDTO>(this.url + '/' + id, datos, {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Elimina un usuario.
   *
   * @param id Identificador del usuario
   * @returns Observable que termina cuando se elimina
   */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.url + '/' + id, {
      headers: this.obtenerHeaders()
    });
  }
}
