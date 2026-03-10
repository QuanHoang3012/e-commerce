import { useState, useCallback, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { CheckoutService } from '../services';
import type { IOrderTrackingResponse } from '../types';

export const useOrderTrackingPage = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<IOrderTrackingResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchOrder = useCallback(async () => {
    if (!orderId) return;
    setLoading(true);
    try {
      const res = await CheckoutService.getOrderTracking(orderId);
      setOrder(res.data);
    } catch {
      setOrder(null);
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    fetchOrder();
  }, [fetchOrder]);

  return { order, loading };
};
