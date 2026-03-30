import React from 'react';
import { Link } from 'react-router-dom';
import { useCheckoutPage } from '../hooks';
import { formatCurrency } from '@shared/utils';
import './CheckoutPage.scss';

export const CheckoutPage: React.FC = () => {
  const { cart, step, loading, error, orderId, handleSubmit, handleViewOrder } = useCheckoutPage();

  if (step === 'success') {
    return (
      <div className="checkout-page container">
        <div className="checkout-page__success">
          <div className="checkout-page__success-icon">✓</div>
          <h1>Đặt hàng thành công!</h1>
          <p>Cảm ơn bạn đã đặt hàng. Đơn hàng của bạn đang được xử lý.</p>
          <div className="checkout-page__success-actions">
            <button className="btn btn-primary btn-lg" onClick={handleViewOrder}>
              Theo dõi đơn hàng
            </button>
            <Link to="/products" className="btn btn-outline">
              Tiếp tục mua sắm
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="checkout-page container">
        <div className="checkout-page__empty">
          <p>Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.</p>
          <Link to="/products" className="btn btn-primary">Mua sắm ngay</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="checkout-page container">
      <h1>Thanh toán</h1>

      {error && <div className="checkout-page__error">{error}</div>}

      <div className="checkout-page__content">
        <form className="checkout-page__form" onSubmit={handleSubmit}>
          <h2>Thông tin giao hàng</h2>

          <div className="checkout-page__field">
            <label htmlFor="customerName">Họ và tên *</label>
            <input type="text" id="customerName" name="customerName" required placeholder="Nhập họ và tên" />
          </div>

          <div className="checkout-page__field">
            <label htmlFor="customerPhone">Số điện thoại *</label>
            <input type="tel" id="customerPhone" name="customerPhone" required placeholder="Nhập số điện thoại" />
          </div>

          <div className="checkout-page__field">
            <label htmlFor="customerEmail">Email *</label>
            <input type="email" id="customerEmail" name="customerEmail" required placeholder="Nhập email" />
          </div>

          <div className="checkout-page__field">
            <label htmlFor="shippingAddress">Địa chỉ giao hàng *</label>
            <textarea id="shippingAddress" name="shippingAddress" required rows={3} placeholder="Nhập địa chỉ giao hàng" />
          </div>

          <div className="checkout-page__field">
            <label>Phương thức thanh toán *</label>
            <div className="checkout-page__payment-methods">
              <label className="checkout-page__payment-option">
                <input type="radio" name="paymentMethod" value="COD" defaultChecked />
                <span>Thanh toán khi nhận hàng (COD)</span>
              </label>
              <label className="checkout-page__payment-option">
                <input type="radio" name="paymentMethod" value="BANK_TRANSFER" />
                <span>Chuyển khoản ngân hàng</span>
              </label>
            </div>
          </div>

          <div className="checkout-page__field">
            <label htmlFor="note">Ghi chú</label>
            <textarea id="note" name="note" rows={2} placeholder="Ghi chú cho đơn hàng (tùy chọn)" />
          </div>

          <button type="submit" className="btn btn-primary btn-lg checkout-page__submit" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Xác nhận đặt hàng'}
          </button>
        </form>

        <div className="checkout-page__order-summary">
          <h2>Tóm tắt đơn hàng</h2>
          <div className="checkout-page__order-items">
            {cart.items.map((item) => (
              <div key={item.id} className="checkout-page__order-item">
                <div className="checkout-page__order-item-image">
                  <img src={item.thumbnail} alt={item.productName} />
                  <span className="checkout-page__order-item-qty">{item.quantity}</span>
                </div>
                <div className="checkout-page__order-item-info">
                  <p className="checkout-page__order-item-name">{item.productName}</p>
                  <p className="checkout-page__order-item-variant">
                    {[item.size, item.color].filter(Boolean).join(' / ')}
                  </p>
                </div>
                <span className="checkout-page__order-item-price">{formatCurrency(item.subTotal)}</span>
              </div>
            ))}
          </div>
          <div className="checkout-page__order-total">
            <span>Tổng cộng</span>
            <span>{formatCurrency(cart.totalAmount)}</span>
          </div>
        </div>
      </div>
    </div>
  );
};
