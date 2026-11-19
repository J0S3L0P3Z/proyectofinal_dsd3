import {ChangeDetectorRef, Component, OnDestroy, inject} from '@angular/core';
import {AdminDatosService} from '../../services/admin-datos.service';
import {RespuestaIA} from '../../services/usuario-datos.service';
import {errorDeUrl, paginaNoResponde} from '../../utils/url';

type EstadoIA = 'esperando' | 'analizando' | 'listo' | 'error';

interface TarjetaIA {
  numero: number;
  icono: string;
  enfoque: string;
  estado: EstadoIA;
  respuesta: RespuestaIA | null;
}

@Component({
  selector: 'app-adminscrapingcomponent',
  standalone: false,
  styleUrl: './adminscrapingcomponent.css',
  templateUrl: './adminscrapingcomponent.html',
})
export class Adminscrapingcomponent implements OnDestroy {

  private datos = inject(AdminDatosService);
  private cdr = inject(ChangeDetectorRef);

  url = '';
  errorUrl = '';
  procesando = false;
  terminado = false;
  fallo = '';

  ias: TarjetaIA[] = [
    {numero: 1, icono: 'fa-brain', enfoque: 'Resumen general', estado: 'esperando', respuesta: null},
    {numero: 2, icono: 'fa-wand-magic-sparkles', enfoque: 'Contenido y personas', estado: 'esperando', respuesta: null},
    {numero: 3, icono: 'fa-infinity', enfoque: 'Tecnología del sitio', estado: 'esperando', respuesta: null},
  ];

  private temporizadores: any[] = [];

  constructor() {

    if (this.datos.urlPendiente) {
      this.url = this.datos.urlPendiente;
      this.datos.urlPendiente = '';
    }
  }

  analizar() {
    const url = this.url.trim();
    this.errorUrl = errorDeUrl(url);
    if (this.errorUrl) return;

    this.limpiarTemporizadores();
    this.procesando = true;
    this.terminado = false;
    this.fallo = '';
    for (const ia of this.ias) {
      ia.estado = 'analizando';
      ia.respuesta = null;
    }


    if (paginaNoResponde(url)) {
      const t = setTimeout(() => {
        for (const ia of this.ias) {
          ia.estado = 'error';
        }
        this.terminar(url, null);
      }, 2000);
      this.temporizadores.push(t);
      return;
    }

    const respuestas = this.datos.respuestasDePrueba(url);
    this.ias.forEach((ia, i) => {

      const t = setTimeout(() => {
        ia.estado = 'listo';
        ia.respuesta = respuestas[i];
        if (i === this.ias.length - 1) {
          this.terminar(url, respuestas);
        }
        this.cdr.markForCheck();
      }, 1500 + i * 1200);
      this.temporizadores.push(t);
    });
  }

  cancelar() {
    this.limpiarTemporizadores();
    this.reiniciarIAs();
    this.procesando = false;
  }

  nuevo() {
    this.reiniciarIAs();
    this.url = '';
    this.terminado = false;
    this.fallo = '';
  }

  ngOnDestroy() {
    this.limpiarTemporizadores();
  }

  private terminar(url: string, respuestas: RespuestaIA[] | null) {
    this.procesando = false;
    this.terminado = true;
    if (!respuestas) {
      this.fallo = 'La página no respondió después de 30 segundos.';
    }
    this.datos.registrar(url, respuestas !== null, respuestas ?? [], this.fallo);
    this.cdr.markForCheck();
  }

  private reiniciarIAs() {
    for (const ia of this.ias) {
      ia.estado = 'esperando';
      ia.respuesta = null;
    }
  }

  private limpiarTemporizadores() {
    for (const t of this.temporizadores) {
      clearTimeout(t);
    }
    this.temporizadores = [];
  }
}
