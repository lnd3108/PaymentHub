import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryDetailPage } from './category-detail-page';

describe('CategoryDetailPage', () => {
  let component: CategoryDetailPage;
  let fixture: ComponentFixture<CategoryDetailPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryDetailPage],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryDetailPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
