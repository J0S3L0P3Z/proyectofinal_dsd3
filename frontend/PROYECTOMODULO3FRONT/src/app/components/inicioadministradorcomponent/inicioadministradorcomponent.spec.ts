import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppModule } from '../../app-module';
import { Inicioadministradorcomponent } from './inicioadministradorcomponent';

describe('Inicioadministradorcomponent', () => {
  let component: Inicioadministradorcomponent;
  let fixture: ComponentFixture<Inicioadministradorcomponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('token', 'token-de-prueba');
    localStorage.setItem('usuario', JSON.stringify({ id: 1, nombre: 'Admin', role: 'ADMIN' }));

    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Inicioadministradorcomponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => localStorage.clear());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('muestra las estadísticas cuando terminan las 6 consultas', async () => {
    const pantalla = fixture.nativeElement as HTMLElement;
    expect(pantalla.textContent).toContain('Cargando estadísticas');

    const peticiones = http.match(() => true);
    expect(peticiones.length).toBe(6);
    peticiones.forEach((peticion) => peticion.flush(peticion.request.url.includes('/contar') ? 3 : []));
    await fixture.whenStable();

    expect(component.cargando).toBe(false);
    expect(component.totalUsuarios).toBe(3);
    expect(pantalla.textContent).not.toContain('Cargando estadísticas');
  });
});
