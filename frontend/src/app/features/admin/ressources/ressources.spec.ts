import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ressources } from './ressources';

describe('Ressources', () => {
  let component: Ressources;
  let fixture: ComponentFixture<Ressources>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Ressources],
    }).compileComponents();

    fixture = TestBed.createComponent(Ressources);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
