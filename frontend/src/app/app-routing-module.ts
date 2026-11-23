import {NgModule, inject} from '@angular/core';
import {RouterModule, Routes, CanActivateFn, Router} from '@angular/router';

import {Logincomponent} from './components/logincomponent/logincomponent';
import {Registrocomponent} from './components/registrocomponent/registrocomponent';
import {Recuperacionpassword} from './components/recuperacionpassword/recuperacionpassword';
import {Inicioadministradorcomponent} from './components/inicioadministradorcomponent/inicioadministradorcomponent';
import {Iniciousuariocomponent} from './components/iniciousuariocomponent/iniciousuariocomponent';
import {Scrapingcomponent} from './components/scrapingcomponent/scrapingcomponent';
import {Historialcomponent} from './components/historialcomponent/historialcomponent';
import {Resultadocomponent} from './components/resultadocomponent/resultadocomponent';
import {Reportecomponent} from './components/reportecomponent/reportecomponent';
import {Gestionusuarioscomponent} from './components/gestionusuarioscomponent/gestionusuarioscomponent';

/**
 * Crea un guard que solo deja entrar a las páginas si hay sesión iniciada y el rol
 * del usuario está permitido. Si el rol no está permitido, lo envía a su propio inicio;
 * si no hay sesión, lo envía al login.
 *
 * @param rolesPermitidos Roles que pueden entrar a la ruta (ADMIN, USER)
 * @returns Guard de activación para la ruta
 */
const protegerRuta = (rolesPermitidos: string[]): CanActivateFn => {
  return () => {

    const router = inject(Router);

    const token = localStorage.getItem('token');
    const usuarioGuardado = localStorage.getItem('usuario');

    if (!token || !usuarioGuardado) {
      return router.createUrlTree(['/login']);
    }

    try {
      const usuario = JSON.parse(usuarioGuardado);
      const rol = (usuario.role || '').toUpperCase();

      if (!rolesPermitidos.includes(rol)) {

        if (rol === 'ADMIN') {
          return router.createUrlTree(['/inicioadministrador']);
        }

        if (rol === 'USER') {
          return router.createUrlTree(['/iniciousuario']);
        }

        return router.createUrlTree(['/login']);
      }

      return true;

    } catch {
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');

      return router.createUrlTree(['/login']);
    }
  };
};

/** Rutas de la aplicación. Login, registro y recuperación son públicas; las demás piden sesión. */
const routes: Routes = [

  {
    path: 'login',
    component: Logincomponent
  },
  {
    path: 'registro',
    component: Registrocomponent
  },
  {
    path: 'recuperacionpassword',
    component: Recuperacionpassword
  },

  {
    path: 'inicioadministrador',
    component: Inicioadministradorcomponent,
    canActivate: [protegerRuta(['ADMIN'])]
  },
  {
    path: 'iniciousuario',
    component: Iniciousuariocomponent,
    canActivate: [protegerRuta(['USER'])]
  },
  {
    path: 'usuarios',
    component: Gestionusuarioscomponent,
    canActivate: [protegerRuta(['ADMIN'])]
  },
  {
    path: 'scraping',
    component: Scrapingcomponent,
    canActivate: [protegerRuta(['ADMIN', 'USER'])]
  },
  {
    path: 'historial',
    component: Historialcomponent,
    canActivate: [protegerRuta(['ADMIN', 'USER'])]
  },
  {
    path: 'resultados/:id',
    component: Resultadocomponent,
    canActivate: [protegerRuta(['ADMIN', 'USER'])]
  },
  {
    path: 'reportes',
    component: Reportecomponent,
    canActivate: [protegerRuta(['ADMIN', 'USER'])]
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];

/** Módulo de rutas de la aplicación. */
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
