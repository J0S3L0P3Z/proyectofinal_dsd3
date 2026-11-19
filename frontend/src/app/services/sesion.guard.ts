import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {SesionService} from './sesion.service';

// Deja entrar solo si hay sesión y el rol coincide con el que pide la ruta (data: { rol: ... })
export const sesionGuard: CanActivateFn = (route) => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  const usuario = sesion.usuarioActual();

  if (!usuario) {
    return router.createUrlTree(['/inicio']);
  }
  if (route.data['rol'] && route.data['rol'] !== usuario.rol) {
    return router.createUrlTree([usuario.rol === 'admin' ? '/admin' : '/usuario']);
  }
  return true;
};
