import { Component, computed, inject, signal } from '@angular/core';
import { EstadoScraping, UsuarioDatosService } from '../../services/usuario-datos.service';

@Component({
  selector: 'app-usuariohistorialcomponent',
  standalone: false,
  styleUrl: './usuariohistorialcomponent.css',
  templateUrl: './usuariohistorialcomponent.html',
})
export class Usuariohistorialcomponent {
  datos = inject(UsuarioDatosService);

  busqueda = signal('');
  filtro = signal<'todos' | EstadoScraping>('todos');

  filtros: { valor: 'todos' | EstadoScraping; texto: string }[] = [
    { valor: 'todos', texto: 'Todos' },
    { valor: 'exitoso', texto: 'Exitosos' },
    { valor: 'fallido', texto: 'Fallidos' },
  ];

  // lista que se muestra en la tabla según el filtro y lo que se busque
  lista = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.datos.historial().filter(a =>
      (this.filtro() === 'todos' || a.estado === this.filtro()) &&
      (!texto || a.url.toLowerCase().includes(texto)),
    );
  });
}
