import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { AppModule } from '../../app-module';
import { of } from 'rxjs';
import { Usuarioresultadoscomponent } from './usuarioresultadoscomponent';

describe('Usuarioresultadoscomponent', () => {
  let component: Usuarioresultadoscomponent;
  let fixture: ComponentFixture<Usuarioresultadoscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [{ provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '106' })) } }],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarioresultadoscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe cargar el análisis indicado en la ruta', () => {
    expect(component.analisis?.id).toBe(106);
  });
});
