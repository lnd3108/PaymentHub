import { Routes } from '@angular/router';
import { CategoryList } from './pages/category/category-list/category-list';
import { CategoryShell } from './pages/category/category-shell/category-shell';
import { CategoryFormPage } from './pages/category/category-form-page/category-form-page';
import { CategoryDetailPage } from './pages/category/category-detail-page/category-detail-page';

export const routes: Routes = [
  {
    path: 'categories',
    component: CategoryShell,

    children: [
      {
        path: '',
        component: CategoryList,
      },
      {
        path: 'create',
        component: CategoryFormPage,
        data: { mode: 'create' },
      },
      {
        path: ':id/edit',
        component: CategoryFormPage,
        data: { mode: 'edit' },
      },
      {
        path: ':id/copy',
        component: CategoryFormPage,
        data: { mode: 'copy' },
      },
      {
        path: ':id/submit',
        component: CategoryDetailPage,
        data: { mode: 'submit' },
      },
      {
        path: ':id/approve',
        component: CategoryDetailPage,
        data: { mode: 'approve' },
      },
      {
        path: ':id/cancel-approve',
        component: CategoryDetailPage,
        data: { mode: 'cancel-approve '},
      },
    ],
  },
  { path: '', redirectTo: 'categories', pathMatch: 'full' },
  { path: '**', redirectTo: 'categories' },
];
