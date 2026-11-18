import {Component, computed, inject} from '@angular/core';
import {UsuarioDatosService} from '../../services/usuario-datos.service';

@Component({
  selector: 'app-usuarioreportescomponent',
  standalone: false,
  styleUrl: './usuarioreportescomponent.css',
  templateUrl: './usuarioreportescomponent.html',
})
export class Usuarioreportescomponent {
  datos = inject(UsuarioDatosService);
  conReporte = computed(() => this.datos.historial().filter(a => a.reporte));
}
