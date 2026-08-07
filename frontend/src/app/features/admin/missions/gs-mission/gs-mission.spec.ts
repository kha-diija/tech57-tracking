import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GsMission } from './gs-mission';

describe('GsMission', () => {
  let component: GsMission;
  let fixture: ComponentFixture<GsMission>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GsMission],
    }).compileComponents();

    fixture = TestBed.createComponent(GsMission);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
