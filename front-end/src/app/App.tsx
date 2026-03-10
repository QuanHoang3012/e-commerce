import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { CartProvider } from '@features/Cart';
import { Header } from './components';
import { AppRoutes } from './routes';
import '@shared/styles/global.scss';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <CartProvider>
        <Header />
        <main>
          <AppRoutes />
        </main>
      </CartProvider>
    </BrowserRouter>
  );
};

export default App;
