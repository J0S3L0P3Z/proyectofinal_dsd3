import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';
import {AdminDatosService, ScrapingAdmin} from '../../services/admin-datos.service';

@Component({
  selector: 'app-adminhistorialcomponent',
  standalone: false,
  styleUrl: './adminhistorialcomponent.css',
  templateUrl: './adminhistorialcomponent.html',
})
export class Adminhistorialcomponent {

  datos = inject(AdminDatosService);
  private router = inject(Router);

  busqueda = '';
  filtro: 'todos' | 'exitosos' | 'fallidos' = 'todos';
  abierto: number | null = null;
  porEliminar: number | null = null;

  get lista(): ScrapingAdmin[] {
    const texto = this.busqueda.trim().toLowerCase();
    return this.datos.misScrapings().filter(s => {
      if (this.filtro === 'exitosos' && !s.exitoso) return false;
      if (this.filtro === 'fallidos' && s.exitoso) return false;
      return !texto || s.url.toLowerCase().includes(texto);
    });
  }

  get total(): number {
    return this.datos.misScrapings().length;
  }

  get exitosos(): number {
    return this.datos.misScrapings().filter(s => s.exitoso).length;
  }

  get fallidos(): number {
    return this.total - this.exitosos;
  }

  verRespuestas(id: number) {
    this.abierto = this.abierto === id ? null : id;
  }

  volverAAnalizar(s: ScrapingAdmin) {
    this.datos.urlPendiente = s.url;
    this.router.navigate(['/admin/scraping']);
  }

  eliminar(id: number) {
    this.datos.eliminar(id);
    this.porEliminar = null;
    if (this.abierto === id) {
      this.abierto = null;
    }
  }
}
