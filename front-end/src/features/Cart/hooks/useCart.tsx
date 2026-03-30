import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import type { ICartView } from '../types';
import { CartService } from '../services';

interface CartContextType {
  cart: ICartView | null;
  loading: boolean;
  fetchCart: () => Promise<void>;
  handleAddToCart: (variantId: string, quantity: number) => Promise<void>;
  handleUpdateQuantity: (itemId: string, quantity: number) => Promise<void>;
  handleRemoveItem: (itemId: string) => Promise<void>;
}

const CartContext = createContext<CartContextType | null>(null);

const transformCart = (dto: { id: string; totalAmount: number; totalItems: number; items: Array<{ id: string; variantId: string; productName: string; skuCode: string; size: string; color: string; thumbnail: string; unitPrice: number; quantity: number; subTotal: number; maxStock: number }> }): ICartView => ({
  id: dto.id,
  totalAmount: dto.totalAmount,
  totalItems: dto.totalItems,
  items: dto.items.map((item) => ({
    id: item.id,
    variantId: item.variantId,
    productName: item.productName,
    skuCode: item.skuCode,
    size: item.size,
    color: item.color,
    thumbnail: item.thumbnail,
    unitPrice: item.unitPrice,
    quantity: item.quantity,
    subTotal: item.subTotal,
    maxStock: item.maxStock,
  })),
});

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cart, setCart] = useState<ICartView | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchCart = useCallback(async () => {
    setLoading(true);
    try {
      const res = await CartService.getCart();
      setCart(transformCart(res.data));
    } catch {
      setCart(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const handleAddToCart = useCallback(async (variantId: string, quantity: number) => {
    try {
      const res = await CartService.addItem({ variantId, quantity });
      setCart(transformCart(res.data));
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Không thể thêm vào giỏ hàng');
    }
  }, []);

  const handleUpdateQuantity = useCallback(async (itemId: string, quantity: number) => {
    try {
      const res = await CartService.updateItem(itemId, quantity);
      setCart(transformCart(res.data));
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Không thể cập nhật số lượng');
    }
  }, []);

  const handleRemoveItem = useCallback(async (itemId: string) => {
    try {
      const res = await CartService.removeItem(itemId);
      setCart(transformCart(res.data));
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Không thể xóa sản phẩm');
    }
  }, []);

  return (
    <CartContext.Provider value={{ cart, loading, fetchCart, handleAddToCart, handleUpdateQuantity, handleRemoveItem }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart must be used within CartProvider');
  return context;
};
