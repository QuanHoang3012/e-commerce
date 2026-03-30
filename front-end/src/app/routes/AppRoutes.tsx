import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from './constants';
import { ProductListPage, ProductDetailPage } from '@features/Product';
import { CartPage } from '@features/Cart';
import { CheckoutPage, OrderTrackingPage } from '@features/Checkout';

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path={ROUTES.HOME} element={<Navigate to={ROUTES.PRODUCTS} replace />} />
      <Route path={ROUTES.PRODUCTS} element={<ProductListPage />} />
      <Route path={ROUTES.PRODUCT_DETAIL} element={<ProductDetailPage />} />
      <Route path={ROUTES.CART} element={<CartPage />} />
      <Route path={ROUTES.CHECKOUT} element={<CheckoutPage />} />
      <Route path={ROUTES.ORDER_TRACKING} element={<OrderTrackingPage />} />
    </Routes>
  );
};
