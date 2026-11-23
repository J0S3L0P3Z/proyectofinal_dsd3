import { Component } from '@angular/core';

/** Pie de página de la aplicación. */
@Component({
  selector: 'app-footercomponent',
  standalone: false,
  templateUrl: './footercomponent.html',
  styleUrl: './footercomponent.css'
})
export class Footercomponent {
  /** Año actual que se muestra en el pie de página. */
  anio = new Date().getFullYear();
}
