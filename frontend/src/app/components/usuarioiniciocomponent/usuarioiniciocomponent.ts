import { Component, computed, inject } from '@angular/core';
import { UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

@Component({
  selector: 'app-usuarioiniciocomponent',
  standalone: false,
  styleUrl: './usuarioiniciocomponent.css',
  templateUrl: './usuarioiniciocomponent.html',
})
export class Usuarioiniciocomponent {
  readonly datos = inject(UsuarioDatosService);
  readonly hoy = new Date();

  readonly primerNombre = computed(() => this.datos.perfil().nombreCompleto.split(' ')[0]);
  readonly recientes = computed(() => this.datos.historial().slice(0, 4));
  readonly tasaExito = computed(() => {
    const total = this.datos.historial().length;
    return total ? Math.round((this.datos.totalExitosos() / total) * 100) : 0;
  });
}
