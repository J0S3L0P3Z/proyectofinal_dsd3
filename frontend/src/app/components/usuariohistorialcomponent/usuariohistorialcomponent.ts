import { Component, computed, inject, signal } from '@angular/core';
import { EstadoScraping, UsuarioDatosService } from '../usuariolayoutcomponent/usuario-datos.service';

/*
 * Historial de scraping del usuario actual.
 * Solo muestra los análisis propios; el backend deberá filtrar por el usuario autenticado.
 */
@Component({
  selector: 'app-usuariohistorialcomponent',
  standalone: false,
  styleUrl: './usuariohistorialcomponent.css',
  templateUrl: './usuariohistorialcomponent.html',
})
export class Usuariohistorialcomponent {
  readonly datos = inject(UsuarioDatosService);

  readonly busqueda = signal('');
  readonly filtro = signal<'todos' | EstadoScraping>('todos');

  readonly filtros: { valor: 'todos' | EstadoScraping; texto: string }[] = [
    { valor: 'todos', texto: 'Todos' },
    { valor: 'exitoso', texto: 'Exitosos' },
    { valor: 'fallido', texto: 'Fallidos' },
  ];

  readonly lista = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.datos.historial().filter(a =>
      (this.filtro() === 'todos' || a.estado === this.filtro()) &&
      (!texto || a.url.toLowerCase().includes(texto)),
    );
  });
}
