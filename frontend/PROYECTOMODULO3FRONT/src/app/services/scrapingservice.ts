import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_BACK } from './authservice';

/** Datos de un scraping (página analizada) tal como los devuelve el backend. */
export interface ScrapingDTO {
  idScraping?: number;
  url?: string;
  tituloPagina?: string;
  contenidoExtraido?: string;
  datosJson?: string;
  cantidadImagenes?: number;
  cantidadEnlaces?: number;
  cantidadPalabras?: number;
  nombresPersonas?: string;
  tecnologiasIdentificadas?: string;
  fechaScraping?: string;
  estado?: string;
  idUsuario?: number;
}

/** Servicio para ejecutar y consultar los scrapings (rutas /scraping del backend). */
@Injectable({
  providedIn: 'root'
})
export class Scrapingservice {

  /** Ruta base de los scrapings en el backend. */
  private url = URL_BACK + '/scraping';

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
   * Ejecuta el scraping de una URL. El backend extrae la página, la analiza con las IA,
   * genera el PDF y envía el correo, por lo que la respuesta puede tardar.
   *
   * @param datos URL a analizar e identificador del usuario
   * @returns Scraping realizado
   */
  realizarScraping(datos: { url: string; idUsuario: number }): Observable<ScrapingDTO> {
    return this.http.post<ScrapingDTO>(
      this.url + '/ejecutar',
      {
        url: datos.url,
        idUsuario: datos.idUsuario,
        cantidadImagenes: 0,
        cantidadEnlaces: 0,
        cantidadPalabras: 0
      },
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta todos los scrapings.
   *
   * @returns Lista de scrapings
   */
  buscarTodos(): Observable<ScrapingDTO[]> {
    return this.http.get<ScrapingDTO[]>(
      this.url,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta un scraping por su identificador.
   *
   * @param id Identificador del scraping
   * @returns Scraping encontrado
   */
  buscarPorId(id: number): Observable<ScrapingDTO> {
    return this.http.get<ScrapingDTO>(
      this.url + '/' + id,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta los scrapings de un usuario.
   *
   * @param idUsuario Identificador del usuario
   * @returns Scrapings del usuario
   */
  buscarPorUsuario(idUsuario: number): Observable<ScrapingDTO[]> {
    return this.http.get<ScrapingDTO[]>(
      this.url + '/usuario/' + idUsuario,
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Consulta los scrapings que tienen un estado.
   *
   * @param estado Estado a buscar (COMPLETADO o ERROR)
   * @returns Scrapings con ese estado
   */
  buscarPorEstado(estado: string): Observable<ScrapingDTO[]> {
    return this.http.get<ScrapingDTO[]>(
      this.url + '/estado/' + encodeURIComponent(estado),
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Elimina un scraping.
   *
   * @param id Identificador del scraping
   * @returns Observable que termina cuando se elimina
   */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(
      this.url + '/' + id,
      { headers: this.obtenerHeaders() }
    );
  }
}
