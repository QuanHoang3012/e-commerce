import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '@features/Cart/hooks';
import './Header.scss';

export const Header: React.FC = () => {
  const { cart } = useCart();
  const totalItems = cart?.totalItems || 0;

  return (
    <header className="header">
      <div className="header__inner container">
        <Link to="/" className="header__logo">
          E-Commerce
        </Link>
        <nav className="header__nav">
          <Link to="/products" className="header__nav-link">Sản phẩm</Link>
        </nav>
        <div className="header__actions">
          <Link to="/cart" className="header__cart">
            🛒
            {totalItems > 0 && <span className="header__cart-badge">{totalItems}</span>}
          </Link>
        </div>
      </div>
    </header>
  );
};
