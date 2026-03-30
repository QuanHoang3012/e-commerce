import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../hooks';
import { CartItem } from '../components';
import { formatCurrency } from '@shared/utils';
import './CartPage.scss';

export const CartPage: React.FC = () => {
  const { cart, loading, handleUpdateQuantity, handleRemoveItem } = useCart();

  if (loading) return <div className="loading-spinner" />;

  return (
    <div className="cart-page container">
      <h1>Giỏ hàng</h1>

      {!cart || cart.items.length === 0 ? (
        <div className="cart-page__empty">
          <p>Giỏ hàng của bạn đang trống.</p>
          <Link to="/products" className="btn btn-primary">
            Tiếp tục mua sắm
          </Link>
        </div>
      ) : (
        <div className="cart-page__content">
          <div className="cart-page__items">
            {cart.items.map((item) => (
              <CartItem
                key={item.id}
                item={item}
                onUpdateQuantity={handleUpdateQuantity}
                onRemove={handleRemoveItem}
              />
            ))}
          </div>

          <div className="cart-page__summary">
            <h2>Tổng đơn hàng</h2>
            <div className="cart-page__summary-row">
              <span>Số lượng sản phẩm</span>
              <span>{cart.totalItems}</span>
            </div>
            <div className="cart-page__summary-row cart-page__summary-total">
              <span>Tổng tiền</span>
              <span>{formatCurrency(cart.totalAmount)}</span>
            </div>
            <Link to="/checkout" className="btn btn-primary btn-lg cart-page__checkout-btn">
              Tiến hành đặt hàng
            </Link>
            <Link to="/products" className="btn btn-outline cart-page__continue-btn">
              Tiếp tục mua sắm
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};
