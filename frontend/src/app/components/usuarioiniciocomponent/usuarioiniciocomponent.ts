import { Component, computed, inject } from '@angular/core';
import { UsuarioDatosService } from '../../services/usuario-datos.service';

@Component({
  selector: 'app-usuarioiniciocomponent',
  standalone: false,
  styleUrl: './usuarioiniciocomponent.css',
  templateUrl: './usuarioiniciocomponent.html',
})
export class Usuarioiniciocomponent {
  datos = inject(UsuarioDatosService);
  hoy = new Date();

  primerNombre = computed(() => this.datos.perfil().nombreCompleto.split(' ')[0]);
  recientes = computed(() => this.datos.historial().slice(0, 4));
  tasaExito = computed(() => {
    const total = this.datos.historial().length;
    return total ? Math.round((this.datos.totalExitosos() / total) * 100) : 0;
  });
}
