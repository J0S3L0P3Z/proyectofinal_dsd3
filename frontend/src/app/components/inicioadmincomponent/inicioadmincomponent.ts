import {Component, inject} from '@angular/core';
import {Cuenta, SesionService} from '../../services/sesion.service';
import {AdminDatosService, ScrapingAdmin} from '../../services/admin-datos.service';

@Component({
  selector: 'app-inicioadmincomponent',
  standalone: false,
  styleUrl: './inicioadmincomponent.css',
  templateUrl: './inicioadmincomponent.html',
})
export class Inicioadmincomponent {

  sesion = inject(SesionService);
  private adminDatos = inject(AdminDatosService);

  accesos = [
    {
      ruta: '/admin/scraping',
      icono: 'fa-bolt',
      color: 'azul',
      titulo: 'Nuevo scraping',
      texto: 'Pega una URL y mira qué dicen las 3 IAs.'
    },
    {
      ruta: '/admin/historial',
      icono: 'fa-clock-rotate-left',
      color: 'cian',
      titulo: 'Mi historial',
      texto: 'Las páginas que ya analizaste.'
    },
    {
      ruta: '/admin/estadisticas',
      icono: 'fa-chart-line',
      color: 'verde',
      titulo: 'Estadísticas',
      texto: 'Usuarios, admins y scrapings en números.'
    },
    {
      ruta: '/admin/usuarios',
      icono: 'fa-users-gear',
      color: 'morado',
      titulo: 'Usuarios',
      texto: 'Revisa las cuentas y cambia los roles.'
    },
    {
      ruta: '/admin/perfil',
      icono: 'fa-user-shield',
      color: 'naranja',
      titulo: 'Mi perfil',
      texto: 'Tus datos y tu contraseña.'
    },
  ];

  get primerNombre(): string {
    const nombre = this.sesion.usuarioActual()?.nombreCompleto ?? 'admin';
    return nombre.split(' ')[0];
  }

  get saludo(): string {
    const hora = new Date().getHours();
    if (hora < 12) return 'Buenos días';
    if (hora < 19) return 'Buenas tardes';
    return 'Buenas noches';
  }

  get fechaHoy(): string {
    const dias = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'];
    const meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto',
      'septiembre', 'octubre', 'noviembre', 'diciembre'];
    const hoy = new Date();
    return dias[hoy.getDay()] + ', ' + hoy.getDate() + ' de ' + meses[hoy.getMonth()] + ' de ' + hoy.getFullYear();
  }

  get totalAdmins(): number {
    return this.sesion.cuentas.filter(c => c.rol === 'admin').length;
  }

  get misScrapings(): number {
    return this.adminDatos.misScrapings().length;
  }

  get scrapingsHoy(): number {
    const hoy = new Date().toDateString();
    return this.adminDatos.misScrapings().filter(s => new Date(s.fecha).toDateString() === hoy).length;
  }

  get ultimosScrapings(): ScrapingAdmin[] {
    return this.adminDatos.misScrapings().slice(0, 4);
  }

  get usuariosNuevos(): Cuenta[] {
    return [...this.sesion.cuentas]
      .sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion))
      .slice(0, 4);
  }
}
