import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Adminestadisticascomponent } from './adminestadisticascomponent';

describe('Adminestadisticascomponent', () => {
  let component: Adminestadisticascomponent;
  let fixture: ComponentFixture<Adminestadisticascomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminestadisticascomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminestadisticascomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
