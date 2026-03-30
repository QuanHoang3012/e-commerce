import React from 'react';
import type { ICartItemView } from '../types';
import { formatCurrency } from '@shared/utils';
import './CartItem.scss';

interface CartItemProps {
  item: ICartItemView;
  onUpdateQuantity: (itemId: string, quantity: number) => void;
  onRemove: (itemId: string) => void;
}

export const CartItem: React.FC<CartItemProps> = ({ item, onUpdateQuantity, onRemove }) => {
  return (
    <div className="cart-item">
      <div className="cart-item__image">
        <img src={item.thumbnail} alt={item.productName} />
      </div>
      <div className="cart-item__info">
        <h3 className="cart-item__name">{item.productName}</h3>
        <p className="cart-item__variant">
          {[item.size, item.color].filter(Boolean).join(' / ')}
        </p>
        <p className="cart-item__price">{formatCurrency(item.unitPrice)}</p>
      </div>
      <div className="cart-item__quantity">
        <button
          onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
          disabled={item.quantity <= 1}
        >
          −
        </button>
        <span>{item.quantity}</span>
        <button
          onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
          disabled={item.quantity >= item.maxStock}
        >
          +
        </button>
      </div>
      <div className="cart-item__subtotal">
        {formatCurrency(item.subTotal)}
      </div>
      <button className="cart-item__remove" onClick={() => onRemove(item.id)} title="Xóa">
        ✕
      </button>
    </div>
  );
};
