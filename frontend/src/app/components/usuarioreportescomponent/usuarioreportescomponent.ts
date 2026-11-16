import { Component, computed, inject } from '@angular/core';
import { UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

/* Reportes PDF generados por los scraping exitosos del usuario. */
@Component({
  selector: 'app-usuarioreportescomponent',
  standalone: false,
  styleUrl: './usuarioreportescomponent.css',
  templateUrl: './usuarioreportescomponent.html',
})
export class Usuarioreportescomponent {
  readonly datos = inject(UsuarioDatosService);
  readonly conReporte = computed(() => this.datos.historial().filter(a => a.reporte));
}
