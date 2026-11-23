import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_BACK } from './authservice';

/** Reporte PDF de un scraping tal como lo devuelve el backend. */
export interface ReporteDTO {
  idReporte?: number;
  nombreArchivo?: string;
  rutaArchivo?: string;
  fechaGeneracion?: string;
  estado?: string;
  idScraping?: number;
}

/** Servicio para generar, consultar y descargar los reportes PDF (rutas /reporte del backend). */
@Injectable({
  providedIn: 'root'
})
export class Reporteservice {

  /** Ruta base de los reportes en el backend. */
  private url = URL_BACK + '/reporte';

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
   * Pide al backend que genere el reporte PDF de un scraping.
   *
   * @param idScraping Identificador del scraping
   * @returns Datos del reporte generado
   */
  generarPDF(idScraping: number): Observable<ReporteDTO> {
    return this.http.post<ReporteDTO>(
      this.url + '/generar/' + idScraping,
      {},
      { headers: this.obtenerHeaders() }
    );
  }

  /**
   * Descarga el archivo PDF de un reporte.
   *
   * @param id Identificador del reporte
   * @returns Archivo PDF como Blob
   */
  descargarPDF(id: number): Observable<Blob> {
    return this.http.get(this.url + '/descargar/' + id, {
      headers: this.obtenerHeaders(),
      responseType: 'blob'
    });
  }

  /**
   * Consulta los reportes generados para un scraping.
   *
   * @param idScraping Identificador del scraping
   * @returns Lista de reportes del scraping
   */
  buscarPorScraping(idScraping: number): Observable<ReporteDTO[]> {
    return this.http.get<ReporteDTO[]>(
      this.url + '/scraping/' + idScraping,
      { headers: this.obtenerHeaders() }
    );
  }
}
