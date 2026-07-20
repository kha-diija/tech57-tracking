import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Etablissements } from './etablissements';

describe('Etablissements', () => {
  let component: Etablissements;
  let fixture: ComponentFixture<Etablissements>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Etablissements],
    }).compileComponents();

    fixture = TestBed.createComponent(Etablissements);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
