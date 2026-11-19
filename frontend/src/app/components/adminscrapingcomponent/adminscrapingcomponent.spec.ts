import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Adminscrapingcomponent } from './adminscrapingcomponent';

describe('Adminscrapingcomponent', () => {
  let component: Adminscrapingcomponent;
  let fixture: ComponentFixture<Adminscrapingcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminscrapingcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminscrapingcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
