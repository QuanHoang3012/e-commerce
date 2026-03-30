import React from 'react';
import { useOrderTrackingPage } from '../hooks';
import { formatCurrency, formatDate } from '@shared/utils';
import './OrderTrackingPage.scss';

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  PAID: 'Đã thanh toán',
  PROCESSING: 'Đang xử lý',
  SHIPPING: 'Đang giao hàng',
  DELIVERED: 'Đã giao hàng',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

const TIMELINE_STEPS = [
  { key: 'pending', label: 'Chờ xác nhận' },
  { key: 'confirmed', label: 'Đã xác nhận' },
  { key: 'paid', label: 'Đã thanh toán' },
  { key: 'processing', label: 'Đang xử lý' },
  { key: 'shipping', label: 'Đang giao' },
  { key: 'delivered', label: 'Đã giao' },
  { key: 'completed', label: 'Hoàn thành' },
] as const;

export const OrderTrackingPage: React.FC = () => {
  const { order, loading } = useOrderTrackingPage();

  if (loading) return <div className="loading-spinner" />;
  if (!order) return <div className="container"><p>Không tìm thấy đơn hàng.</p></div>;

  return (
    <div className="order-tracking container">
      <h1>Chi tiết đơn hàng</h1>

      <div className="order-tracking__header">
        <div>
          <span className="order-tracking__label">Mã đơn hàng</span>
          <span className="order-tracking__tracking-number">{order.trackingNumber}</span>
        </div>
        <span className={`order-tracking__status order-tracking__status--${order.status.toLowerCase()}`}>
          {STATUS_LABELS[order.status] || order.status}
        </span>
      </div>

      {order.status !== 'CANCELLED' && (
        <div className="order-tracking__timeline">
          {TIMELINE_STEPS.map((s) => {
            const step = order.timeline[s.key as keyof typeof order.timeline];
            return (
              <div key={s.key} className={`order-tracking__timeline-step ${step?.completed ? 'order-tracking__timeline-step--completed' : ''}`}>
                <div className="order-tracking__timeline-dot" />
                <div className="order-tracking__timeline-info">
                  <span className="order-tracking__timeline-label">{s.label}</span>
                  {step?.timestamp && (
                    <span className="order-tracking__timeline-time">{formatDate(step.timestamp)}</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      <div className="order-tracking__sections">
        <div className="order-tracking__section">
          <h2>Thông tin khách hàng</h2>
          <div className="order-tracking__info-grid">
            <div><span>Họ tên:</span> {order.customerName}</div>
            <div><span>Điện thoại:</span> {order.customerPhone}</div>
            <div><span>Email:</span> {order.customerEmail}</div>
            <div><span>Địa chỉ:</span> {order.shippingAddress}</div>
            <div><span>Thanh toán:</span> {order.paymentMethod === 'COD' ? 'Thanh toán khi nhận hàng' : 'Chuyển khoản ngân hàng'}</div>
          </div>
        </div>

        <div className="order-tracking__section">
          <h2>Sản phẩm</h2>
          <div className="order-tracking__items">
            {order.items.map((item, idx) => (
              <div key={idx} className="order-tracking__item">
                <div className="order-tracking__item-info">
                  <p className="order-tracking__item-name">{item.productName}</p>
                  <p className="order-tracking__item-variant">{item.variantInfo} | {item.skuCode}</p>
                </div>
                <div className="order-tracking__item-qty">x{item.quantity}</div>
                <div className="order-tracking__item-price">{formatCurrency(item.subtotal)}</div>
              </div>
            ))}
          </div>
          <div className="order-tracking__total">
            <span>Tổng cộng</span>
            <span>{formatCurrency(order.totalAmount)}</span>
          </div>
        </div>
      </div>
    </div>
  );
};
