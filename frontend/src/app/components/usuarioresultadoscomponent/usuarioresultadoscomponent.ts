import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AnalisisScraping, UsuarioDatosService } from '../../services/usuario-datos.service';

@Component({
  selector: 'app-usuarioresultadoscomponent',
  standalone: false,
  styleUrl: './usuarioresultadoscomponent.css',
  templateUrl: './usuarioresultadoscomponent.html',
})
export class Usuarioresultadoscomponent {
  datos = inject(UsuarioDatosService);
  analisis: AnalisisScraping | undefined;

  constructor() {
    inject(ActivatedRoute).paramMap.subscribe(params => {
      this.analisis = this.datos.obtenerAnalisis(Number(params.get('id')));
    });
  }

  iconoIA(numero: number): string {
    if (numero === 1) return 'fa-brain';
    if (numero === 2) return 'fa-wand-magic-sparkles';
    return 'fa-infinity';
  }
}
