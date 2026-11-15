import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Crearcuentacomponent } from './crearcuentacomponent';

describe('Crearcuentacomponent', () => {
  let component: Crearcuentacomponent;
  let fixture: ComponentFixture<Crearcuentacomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Crearcuentacomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Crearcuentacomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
