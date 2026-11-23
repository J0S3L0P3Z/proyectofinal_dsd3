import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_BACK } from './authservice';

/** Respuesta de una inteligencia artificial sobre un scraping, tal como la devuelve el backend. */
export interface ResultadoDTO {
  idResultado?: number;
  nombreIA?: string;
  modelo?: string;
  respuesta?: string;
  fechaAnalisis?: string;
  estado?: string;
  idScraping?: number;
}

/**
 * Servicio para consultar los resultados de las inteligencias artificiales (rutas /resultado del backend).
 */
@Injectable({
  providedIn: 'root'
})
export class Resultadoservice {

  /** Ruta base de los resultados en el backend. */
  private url = URL_BACK + '/resultado';

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
   * Consulta todos los resultados de las IA.
   *
   * @returns Lista de resultados
   */
  buscarTodos(): Observable<ResultadoDTO[]> {
    return this.http.get<ResultadoDTO[]>(
      this.url,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta un resultado por su identificador.
   *
   * @param id Identificador del resultado
   * @returns Resultado de la IA
   */
  buscarPorId(id: number): Observable<ResultadoDTO> {
    return this.http.get<ResultadoDTO>(
      this.url + '/' + id,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta las respuestas de las IA para un scraping.
   *
   * @param idScraping Identificador del scraping
   * @returns Resultados del scraping
   */
  buscarPorScraping(idScraping: number): Observable<ResultadoDTO[]> {
    return this.http.get<ResultadoDTO[]>(
      this.url + '/scraping/' + idScraping,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta los resultados de una IA en particular.
   *
   * @param nombreIA Nombre de la IA (por ejemplo GEMINI_GENERAL)
   * @returns Resultados de esa IA
   */
  buscarPorNombreIA(nombreIA: string): Observable<ResultadoDTO[]> {
    return this.http.get<ResultadoDTO[]>(
      this.url + '/ia/' + encodeURIComponent(nombreIA),
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta los resultados que tienen un estado.
   *
   * @param estado Estado a buscar
   * @returns Resultados con ese estado
   */
  buscarPorEstado(estado: string): Observable<ResultadoDTO[]> {
    return this.http.get<ResultadoDTO[]>(
      this.url + '/estado/' + encodeURIComponent(estado),
      { headers: this.obtenerHeaders() }
    );
  }
}
