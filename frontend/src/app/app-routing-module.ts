import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {Iniciocomponent} from './components/iniciocomponent/iniciocomponent';
import {Crearcuentacomponent} from './components/crearcuentacomponent/crearcuentacomponent';
import {Inicioadmincomponent} from './components/inicioadmincomponent/inicioadmincomponent';
<<<<<<< Updated upstream
=======
import {sesionGuard} from './services/sesion.guard';
// Apartado Usuario
import {Usuariolayoutcomponent} from './components/usuariolayoutcomponent/usuariolayoutcomponent';
import {Usuarioiniciocomponent} from './components/usuarioiniciocomponent/usuarioiniciocomponent';
import {Usuarioscrapingcomponent} from './components/usuarioscrapingcomponent/usuarioscrapingcomponent';
import {Usuarioresultadoscomponent} from './components/usuarioresultadoscomponent/usuarioresultadoscomponent';
import {Usuariohistorialcomponent} from './components/usuariohistorialcomponent/usuariohistorialcomponent';
import {Usuarioreportescomponent} from './components/usuarioreportescomponent/usuarioreportescomponent';
import {Usuarioperfilcomponent} from './components/usuarioperfilcomponent/usuarioperfilcomponent';
// Apartado Admin
import {Gestionusuarioscomponent} from './components/admingestionusuarioscomponent/gestionusuarioscomponent';
import {Adminscrapingcomponent} from './components/adminscrapingcomponent/adminscrapingcomponent';
import {Adminhistorialcomponent} from './components/adminhistorialcomponent/adminhistorialcomponent';
import {Adminestadisticascomponent} from './components/adminestadisticascomponent/adminestadisticascomponent';
import {Adminperfilcomponent} from './components/adminperfilcomponent/adminperfilcomponent';
>>>>>>> Stashed changes


const routes: Routes = [

<<<<<<< Updated upstream
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'inicio', component: Iniciocomponent },
  { path: 'crearcuentacomponent', component: Crearcuentacomponent },
  { path: 'inicioadmin', component: Inicioadmincomponent}
=======
  {path: '', redirectTo: 'inicio', pathMatch: 'full'},
  {path: 'inicio', component: Iniciocomponent},
  {path: 'crearcuentacomponent', component: Crearcuentacomponent},

  // Apartado Admin (todas las páginas piden sesión de admin)
  {
    path: 'admin',
    canActivate: [sesionGuard],
    data: {rol: 'admin'},
    children: [
      {path: '', redirectTo: 'inicio', pathMatch: 'full'},
      {path: 'inicio', component: Inicioadmincomponent},
      {path: 'scraping', component: Adminscrapingcomponent},
      {path: 'historial', component: Adminhistorialcomponent},
      {path: 'estadisticas', component: Adminestadisticascomponent},
      {path: 'usuarios', component: Gestionusuarioscomponent},
      {path: 'perfil', component: Adminperfilcomponent},
    ],
  },
  // rutas viejas, por si alguien las tiene guardadas
  {path: 'inicioadmin', redirectTo: 'admin/inicio', pathMatch: 'full'},
  {path: 'gestionusuario', redirectTo: 'admin/usuarios', pathMatch: 'full'},

  // Apartado Usuario
  {
    path: 'usuario',
    component: Usuariolayoutcomponent,
    canActivate: [sesionGuard],
    data: {rol: 'usuario'},
    children: [
      {path: '', redirectTo: 'inicio', pathMatch: 'full'},
      {path: 'inicio', component: Usuarioiniciocomponent},
      {path: 'scraping', component: Usuarioscrapingcomponent},
      {path: 'resultados/:id', component: Usuarioresultadoscomponent},
      {path: 'historial', component: Usuariohistorialcomponent},
      {path: 'reportes', component: Usuarioreportescomponent},
      {path: 'perfil', component: Usuarioperfilcomponent},
    ],
  },
>>>>>>> Stashed changes

  // Cualquier ruta que no exista vuelve al inicio
  {path: '**', redirectTo: 'inicio'},

];

@NgModule({
  // al cambiar de página vuelve arriba
  imports: [RouterModule.forRoot(routes, {scrollPositionRestoration: 'top'})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
