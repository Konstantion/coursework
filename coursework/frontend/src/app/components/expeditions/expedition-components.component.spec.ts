import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ExpeditionComponents} from './expedition-components.component';

describe('TablesComponent', () => {
  let component: ExpeditionComponents;
  let fixture: ComponentFixture<ExpeditionComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExpeditionComponents]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ExpeditionComponents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
