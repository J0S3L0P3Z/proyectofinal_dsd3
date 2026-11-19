import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Adminperfilcomponent } from './adminperfilcomponent';

describe('Adminperfilcomponent', () => {
  let component: Adminperfilcomponent;
  let fixture: ComponentFixture<Adminperfilcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminperfilcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminperfilcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
