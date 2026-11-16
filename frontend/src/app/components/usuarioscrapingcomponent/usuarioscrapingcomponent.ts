import { Component, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AnalisisScraping, UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

type EstadoVista = 'inactivo' | 'procesando' | 'exitoso' | 'error';

/*
 * Interfaz para iniciar un scraping.
 * El procesamiento es SOLO VISUAL (temporizadores); no hay Selenium ni IA reales.
 * Al conectar Spring Boot, iniciarScraping() debe llamar al endpoint correspondiente
 * y actualizar "pasoActual" con el progreso que devuelva el backend.
 */
@Component({
  selector: 'app-usuarioscrapingcomponent',
  standalone: false,
  styleUrl: './usuarioscrapingcomponent.css',
  templateUrl: './usuarioscrapingcomponent.html',
})
export class Usuarioscrapingcomponent implements OnDestroy {
  readonly datos = inject(UsuarioDatosService);

  url = '';
  errorUrl = '';
  estado: EstadoVista = 'inactivo';
  pasoActual = 0;
  resultado: AnalisisScraping | null = null;

  /** Solo para la vista previa: permite ver el estado de error. */
  simularError = false;

  readonly pasos = [
    { icono: 'fa-link', texto: 'Validando la URL' },
    { icono: 'fa-spider', texto: 'Extrayendo el contenido de la página' },
    { icono: 'fa-brain', texto: 'Analizando con IA 1' },
    { icono: 'fa-wand-magic-sparkles', texto: 'Analizando con IA 2' },
    { icono: 'fa-infinity', texto: 'Analizando con IA 3' },
    { icono: 'fa-file-pdf', texto: 'Generando el reporte PDF' },
  ];

  private temporizador?: ReturnType<typeof setInterval>;

  constructor() {
    // Permite "Reintentar" desde un análisis fallido (?url=...).
    const urlPrevia = inject(ActivatedRoute).snapshot.queryParamMap.get('url');
    if (urlPrevia) this.url = urlPrevia;
  }

  get progreso(): number {
    return Math.round((this.pasoActual / this.pasos.length) * 100);
  }

  iniciarScraping(): void {
    const url = this.url.trim();
    if (!url) {
      this.errorUrl = 'Ingresa la URL de la página que deseas analizar.';
      return;
    }
    if (!/^https?:\/\/[^\s/$.?#].[^\s]*$/i.test(url)) {
      this.errorUrl = 'La URL no es válida. Debe comenzar con http:// o https://';
      return;
    }

    this.errorUrl = '';
    this.resultado = null;
    this.estado = 'procesando';
    this.pasoActual = 0;

    const pasoFallo = 2; // el error simulado ocurre durante la extracción
    this.temporizador = setInterval(() => {
      if (this.simularError && this.pasoActual === pasoFallo - 1) {
        this.finalizar(url, false);
        return;
      }
      this.pasoActual++;
      if (this.pasoActual >= this.pasos.length) {
        this.finalizar(url, true);
      }
    }, 900);
  }

  cancelar(): void {
    this.detener();
    this.estado = 'inactivo';
    this.pasoActual = 0;
    this.datos.notificar('info', 'Scraping cancelado', 'Cancelaste el análisis en curso.', false);
  }

  nuevo(): void {
    this.estado = 'inactivo';
    this.resultado = null;
    this.pasoActual = 0;
    this.url = '';
  }

  ngOnDestroy(): void {
    this.detener();
  }

  private finalizar(url: string, exitoso: boolean): void {
    this.detener();
    this.resultado = this.datos.registrarAnalisisDemo(url, exitoso);
    this.estado = exitoso ? 'exitoso' : 'error';
  }

  private detener(): void {
    if (this.temporizador) clearInterval(this.temporizador);
    this.temporizador = undefined;
  }
}
