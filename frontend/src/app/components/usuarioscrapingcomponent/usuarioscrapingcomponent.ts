import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AnalisisScraping, UsuarioDatosService } from '../../services/usuario-datos.service';
import { errorDeUrl, paginaNoResponde } from '../../utils/url';

@Component({
  selector: 'app-usuarioscrapingcomponent',
  standalone: false,
  styleUrl: './usuarioscrapingcomponent.css',
  templateUrl: './usuarioscrapingcomponent.html',
})
export class Usuarioscrapingcomponent implements OnDestroy {
  datos = inject(UsuarioDatosService);
  private cdr = inject(ChangeDetectorRef);

  url = '';
  errorUrl = '';
  estado: 'inactivo' | 'procesando' | 'exitoso' | 'error' = 'inactivo';
  pasoActual = 0;
  resultado: AnalisisScraping | null = null;

  pasos = [
    { icono: 'fa-link', texto: 'Validando la URL' },
    { icono: 'fa-spider', texto: 'Extrayendo el contenido de la página' },
    { icono: 'fa-brain', texto: 'Analizando con IA 1' },
    { icono: 'fa-wand-magic-sparkles', texto: 'Analizando con IA 2' },
    { icono: 'fa-infinity', texto: 'Analizando con IA 3' },
    { icono: 'fa-file-pdf', texto: 'Generando el reporte PDF' },
  ];

  private temporizador: any;

  constructor() {
    // Si viene desde "Reintentar" de un análisis fallido trae la url en ?url=
    const urlAnterior = inject(ActivatedRoute).snapshot.queryParamMap.get('url');
    if (urlAnterior) {
      this.url = urlAnterior;
    }
  }

  get progreso(): number {
    return Math.round((this.pasoActual / this.pasos.length) * 100);
  }

  iniciarScraping(): void {
    const url = this.url.trim();
    this.errorUrl = errorDeUrl(url);
    if (this.errorUrl) return;

    this.resultado = null;
    this.estado = 'procesando';
    this.pasoActual = 0;


    // con el back pero ps por ahora no esta conectado hay que pedirle el progreso real
    const vaAFallar = paginaNoResponde(url);
    this.temporizador = setInterval(() => {
      if (vaAFallar && this.pasoActual === 1) {
        // falla en el paso de "Extrayendo el contenido"
        this.finalizar(url, false);
      } else {
        this.pasoActual++;
        if (this.pasoActual >= this.pasos.length) {
          this.finalizar(url, true);
        }
      }
      this.cdr.markForCheck();
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
    this.resultado = this.datos.registrarAnalisis(url, exitoso);
    this.estado = exitoso ? 'exitoso' : 'error';
  }

  private detener(): void {
    clearInterval(this.temporizador);
    this.temporizador = null;
  }
}
