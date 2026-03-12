import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryFormPage } from './category-form-page';

describe('CategoryFormPage', () => {
  let component: CategoryFormPage;
  let fixture: ComponentFixture<CategoryFormPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryFormPage],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryFormPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
