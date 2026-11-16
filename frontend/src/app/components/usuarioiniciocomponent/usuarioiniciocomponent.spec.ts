import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModule } from '../../app-module';

import { Usuarioiniciocomponent } from './usuarioiniciocomponent';

describe('Usuarioiniciocomponent', () => {
  let component: Usuarioiniciocomponent;
  let fixture: ComponentFixture<Usuarioiniciocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarioiniciocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe calcular la tasa de éxito entre 0 y 100', () => {
    expect(component.tasaExito()).toBeGreaterThanOrEqual(0);
    expect(component.tasaExito()).toBeLessThanOrEqual(100);
  });
});
