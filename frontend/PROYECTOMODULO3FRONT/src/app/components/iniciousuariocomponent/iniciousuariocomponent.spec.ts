import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppModule } from '../../app-module';
import { Iniciousuariocomponent } from './iniciousuariocomponent';

const scraping = { idScraping: 5, url: 'https://sitio.com', estado: 'COMPLETADO', fechaScraping: '2026-11-20T10:00:00', idUsuario: 7 };

describe('Iniciousuariocomponent', () => {
  let component: Iniciousuariocomponent;
  let fixture: ComponentFixture<Iniciousuariocomponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('token', 'token-de-prueba');
    localStorage.setItem('usuario', JSON.stringify({ id: 7, nombre: 'Ana Gómez', role: 'USER' }));

    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Iniciousuariocomponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => localStorage.clear());

  function responderTodo(): void {
    for (let vuelta = 0; vuelta < 5; vuelta++) {
      http.match(() => true).forEach((peticion) => {
        peticion.flush(peticion.request.url.includes('/scraping/usuario/') ? [scraping] : []);
      });
    }
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deja de mostrar "cargando" cuando el backend responde', async () => {
    const pantalla = fixture.nativeElement as HTMLElement;
    expect(pantalla.textContent).toContain('Cargando tu información');

    responderTodo();
    await fixture.whenStable();

    expect(component.cargando).toBe(false);
    expect(pantalla.textContent).not.toContain('Cargando tu información');
  });

  it('cuenta como exitoso el estado COMPLETADO del backend', () => {
    responderTodo();

    expect(component.totalExitosos()).toBe(1);
    expect(component.totalFallidos()).toBe(0);
    expect(component.tasaExito()).toBe(100);
  });
});
