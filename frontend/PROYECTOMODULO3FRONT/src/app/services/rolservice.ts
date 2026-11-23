import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_BACK } from './authservice';

/** Rol del sistema tal como lo devuelve el backend. */
export interface RolDTO {
  id?: number;
  nombre?: string;
}

/**
 * Servicio para consultar los roles del sistema (rutas /rol del backend, solo para administradores).
 */
@Injectable({
  providedIn: 'root'
})
export class Rolservice {

  /** Ruta base de los roles en el backend. */
  private url = URL_BACK + '/rol';

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
   * Consulta todos los roles.
   *
   * @returns Lista de roles
   */
  buscarTodos(): Observable<RolDTO[]> {
    return this.http.get<RolDTO[]>(this.url + '/buscarTodos', {
      headers: this.obtenerHeaders()
    });
  }

  /**
   * Cuenta cuántos roles hay registrados.
   *
   * @returns Cantidad de roles
   */
  contar(): Observable<number> {
    return this.http.get<number>(this.url + '/contar', {
      headers: this.obtenerHeaders()
    });
  }
}
