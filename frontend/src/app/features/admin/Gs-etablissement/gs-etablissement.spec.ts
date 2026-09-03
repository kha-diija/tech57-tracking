import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GsEtablissement } from './gs-etablissement';

describe('GsEtablissement', () => {
  let component: GsEtablissement;
  let fixture: ComponentFixture<GsEtablissement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GsEtablissement],
    }).compileComponents();

    fixture = TestBed.createComponent(GsEtablissement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
