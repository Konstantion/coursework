import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CampComponent} from './camp.component';

describe('HallComponent', () => {
  let component: CampComponent;
  let fixture: ComponentFixture<CampComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CampComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CampComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
