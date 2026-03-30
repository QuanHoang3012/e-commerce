import { api } from '@shared/api';
import type { ApiResponse } from '@shared/types';
import type { ICartDto, IAddToCartRequest } from '../types';

export class CartService {
  static async getCart(): Promise<ApiResponse<ICartDto>> {
    return api.get('/cart');
  }

  static async addItem(data: IAddToCartRequest): Promise<ApiResponse<ICartDto>> {
    return api.post('/cart/items', data);
  }

  static async updateItem(itemId: string, quantity: number): Promise<ApiResponse<ICartDto>> {
    return api.put(`/cart/items/${encodeURIComponent(itemId)}`, { itemId, quantity });
  }

  static async removeItem(itemId: string): Promise<ApiResponse<ICartDto>> {
    return api.delete(`/cart/items/${encodeURIComponent(itemId)}`);
  }
}
