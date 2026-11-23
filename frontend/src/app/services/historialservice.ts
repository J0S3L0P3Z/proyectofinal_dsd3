import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_BACK } from './authservice';

/** Registro del historial de acciones de un usuario tal como lo devuelve el backend. */
export interface HistorialDTO {
  idHistorial?: number;
  fechaConsulta?: string;
  descripcion?: string;
  estado?: string;
  idUsuario?: number;
  idScraping?: number;
}

/** Servicio para consultar el historial de acciones (rutas /historial del backend). */
@Injectable({
  providedIn: 'root'
})
export class Historialservice {

  /** Ruta base del historial en el backend. */
  private url = URL_BACK + '/historial';

  /**
   * Inyecta el cliente HTTP para comunicarse con el backend.
   *
   * @param http Cliente HTTP de Angular
   */
  constructor(private http: HttpClient) {}

  /**
   * Arma la cabecera Authorization con el token JWT guardado al iniciar sesión.
   *
   * @returns Cabeceras HTTP con el token
   */
  private obtenerHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');

    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  /**
   * Consulta todos los registros del historial.
   *
   * @returns Lista de registros del historial
   */
  buscarTodos(): Observable<HistorialDTO[]> {
    return this.http.get<HistorialDTO[]>(
      this.url,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta un registro del historial por su identificador.
   *
   * @param id Identificador del registro
   * @returns Registro del historial
   */
  buscarPorId(id: number): Observable<HistorialDTO> {
    return this.http.get<HistorialDTO>(
      this.url + '/' + id,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta el historial de un usuario.
   *
   * @param idUsuario Identificador del usuario
   * @returns Registros del historial del usuario
   */
  buscarPorUsuario(idUsuario: number): Observable<HistorialDTO[]> {
    return this.http.get<HistorialDTO[]>(
      this.url + '/usuario/' + idUsuario,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta el historial asociado a un scraping.
   *
   * @param idScraping Identificador del scraping
   * @returns Registros del historial del scraping
   */
  buscarPorScraping(idScraping: number): Observable<HistorialDTO[]> {
    return this.http.get<HistorialDTO[]>(
      this.url + '/scraping/' + idScraping,
      { headers: this.obtenerHeaders() }
    );
  }
}
