import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterTestingHarness } from '@angular/router/testing';
import { AppModule } from '../../app-module';
import { Resultadocomponent } from './resultadocomponent';

const scraping = {
  idScraping: 5, url: 'https://sitio.com', tituloPagina: 'Sitio de prueba', estado: 'COMPLETADO',
  fechaScraping: '2026-11-20T10:00:00', idUsuario: 7, nombresPersonas: 'Ana Gómez', tecnologiasIdentificadas: 'Angular',
};

describe('Resultadocomponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('token', 'token-de-prueba');
    localStorage.setItem('usuario', JSON.stringify({ id: 7, nombre: 'Ana', role: 'USER' }));

    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => localStorage.clear());

  it('muestra el resultado del análisis cuando el backend responde', async () => {
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/resultados/5', Resultadocomponent);
    const pantalla = harness.routeNativeElement as HTMLElement;
    expect(pantalla.textContent).toContain('Cargando resultados');

    http.expectOne((peticion) => peticion.url.endsWith('/scraping/5')).flush(scraping);
    http.match(() => true).forEach((peticion) => peticion.flush([]));
    await harness.fixture.whenStable();

    expect(component.cargando).toBe(false);
    expect(component.analisis?.idScraping).toBe(5);
    expect(pantalla.textContent).not.toContain('Cargando resultados');
    expect(pantalla.textContent).toContain('Ana Gómez');
  });
});
