import { TestBed } from '@angular/core/testing';
import { Scrapingservice } from './scrapingservice';

describe('Scrapingservice', () => {
  let service: Scrapingservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Scrapingservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
