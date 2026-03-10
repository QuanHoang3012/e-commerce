import { useState, useCallback, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '@features/Cart/hooks';
import { CheckoutService } from '../services';
import type { ICheckoutInitiateResponse } from '../types';

export const useCheckoutPage = () => {
  const navigate = useNavigate();
  const { cart, fetchCart } = useCart();
  const [step, setStep] = useState<'form' | 'success'>('form');
  const [loading, setLoading] = useState(false);
  const [reservation, setReservation] = useState<ICheckoutInitiateResponse | null>(null);
  const [orderId, setOrderId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleInitiateCheckout = useCallback(async () => {
    if (!cart) return;
    setLoading(true);
    setError(null);
    try {
      const res = await CheckoutService.initiate(cart.id);
      setReservation(res.data);
      return res.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể khởi tạo đơn hàng');
      return null;
    } finally {
      setLoading(false);
    }
  }, [cart]);

  const handleSubmit = useCallback(async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Step 1: Initiate checkout
      let currentReservation = reservation;
      if (!currentReservation) {
        const initiated = await handleInitiateCheckout();
        if (!initiated) return;
        currentReservation = initiated;
      }

      // Step 2: Confirm order
      const formData = new FormData(e.currentTarget);
      const confirmData = {
        customerName: formData.get('customerName') as string,
        customerPhone: formData.get('customerPhone') as string,
        customerEmail: formData.get('customerEmail') as string,
        shippingAddress: formData.get('shippingAddress') as string,
        paymentMethod: formData.get('paymentMethod') as 'COD' | 'BANK_TRANSFER',
        note: (formData.get('note') as string) || undefined,
      };

      const res = await CheckoutService.confirm(currentReservation.orderId, confirmData);
      setOrderId(res.data.orderId);
      setStep('success');
      fetchCart();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể hoàn tất đơn hàng');
    } finally {
      setLoading(false);
    }
  }, [reservation, handleInitiateCheckout, fetchCart]);

  const handleViewOrder = () => {
    if (orderId) navigate(`/orders/${orderId}`);
  };

  return {
    cart,
    step,
    loading,
    error,
    orderId,
    handleSubmit,
    handleViewOrder,
  };
};
