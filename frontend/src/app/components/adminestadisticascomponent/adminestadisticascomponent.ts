import {Component, inject} from '@angular/core';
import {Cuenta, SesionService} from '../../services/sesion.service';
import {AdminDatosService} from '../../services/admin-datos.service';
import {UsuarioDatosService} from '../../services/usuario-datos.service';

@Component({
  selector: 'app-adminestadisticascomponent',
  standalone: false,
  styleUrl: './adminestadisticascomponent.css',
  templateUrl: './adminestadisticascomponent.html',
})
export class Adminestadisticascomponent {

  sesion = inject(SesionService);
  private adminDatos = inject(AdminDatosService);
  private usuarioDatos = inject(UsuarioDatosService);

  get admins(): Cuenta[] {
    return this.sesion.cuentas.filter(c => c.rol === 'admin');
  }

  get usuarios(): Cuenta[] {
    return this.sesion.cuentas.filter(c => c.rol === 'usuario');
  }

  get porcentajeAdmins(): number {
    const total = this.sesion.cuentas.length;
    return total ? Math.round((this.admins.length / total) * 100) : 0;
  }

  get scrapingsUsuarios(): number {
    return this.usuarioDatos.historial().length;
  }

  get scrapingsAdmins(): number {
    return this.adminDatos.todos().length;
  }

  get totalScrapings(): number {
    return this.scrapingsUsuarios + this.scrapingsAdmins;
  }

  get exitosos(): number {
    return this.usuarioDatos.totalExitosos() + this.adminDatos.todos().filter(s => s.exitoso).length;
  }

  get porcentajeExito(): number {
    return this.totalScrapings ? Math.round((this.exitosos / this.totalScrapings) * 100) : 0;
  }


  get nuevosSemana(): number {
    const haceUnaSemana = Date.now() - 7 * 24 * 60 * 60 * 1000;
    return this.sesion.cuentas.filter(c => new Date(c.fechaCreacion).getTime() >= haceUnaSemana).length;
  }
}
