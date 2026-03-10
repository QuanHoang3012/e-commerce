import { api, checkoutApi } from '@shared/api';
import type { ApiResponse } from '@shared/types';
import type {
  ICheckoutInitiateResponse,
  ICheckoutConfirmRequest,
  ICheckoutConfirmResponse,
  IOrderTrackingResponse,
} from '../types';

export class CheckoutService {
  static async initiate(cartId: string): Promise<ApiResponse<ICheckoutInitiateResponse>> {
    return checkoutApi.post(`/checkout/initiate?cartId=${encodeURIComponent(cartId)}`);
  }

  static async confirm(orderId: string, data: ICheckoutConfirmRequest): Promise<ApiResponse<ICheckoutConfirmResponse>> {
    return checkoutApi.post(`/checkout/confirm?orderId=${encodeURIComponent(orderId)}`, data);
  }

  static async cancel(orderId: string): Promise<ApiResponse<null>> {
    return checkoutApi.post(`/checkout/cancel?orderId=${encodeURIComponent(orderId)}`);
  }

  static async getOrderTracking(orderId: string): Promise<ApiResponse<IOrderTrackingResponse>> {
    return api.get(`/orders/${encodeURIComponent(orderId)}`);
  }
}
