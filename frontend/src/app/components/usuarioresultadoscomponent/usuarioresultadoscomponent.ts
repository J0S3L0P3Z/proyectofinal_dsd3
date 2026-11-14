import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AnalisisScraping, UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

@Component({
  selector: 'app-usuarioresultadoscomponent',
  standalone: false,
  styleUrl: './usuarioresultadoscomponent.css',
  templateUrl: './usuarioresultadoscomponent.html',
})
export class Usuarioresultadoscomponent {
  readonly datos = inject(UsuarioDatosService);
  analisis: AnalisisScraping | undefined;

  /** Estilo visual de cada IA (mismos íconos y colores del panel existente). */
  readonly estiloIA: Record<number, { icono: string; clase: string }> = {
    1: { icono: 'fa-brain', clase: 'ia-1' },
    2: { icono: 'fa-wand-magic-sparkles', clase: 'ia-2' },
    3: { icono: 'fa-infinity', clase: 'ia-3' },
  };

  constructor() {
    // Se lee el id cada vez que cambia la ruta (p. ej. /usuario/resultados/106).
    inject(ActivatedRoute).paramMap.subscribe(p => {
      this.analisis = this.datos.obtenerAnalisis(Number(p.get('id')));
    });
  }
}
