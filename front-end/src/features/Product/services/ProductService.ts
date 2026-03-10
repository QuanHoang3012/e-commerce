import { api } from '@shared/api';
import type { ApiResponse, PaginatedResponse } from '@shared/types';
import type { IProductDto, IProductDetailDto, IProductFilter } from '../types';

export class ProductService {
  static async getList(filter: IProductFilter = {}): Promise<ApiResponse<PaginatedResponse<IProductDto>>> {
    const params = new URLSearchParams();
    if (filter.page) params.append('page', String(filter.page));
    if (filter.size) params.append('size', String(filter.size));
    if (filter.category) params.append('category', filter.category);
    if (filter.minPrice != null) params.append('minPrice', String(filter.minPrice));
    if (filter.maxPrice != null) params.append('maxPrice', String(filter.maxPrice));
    if (filter.sort) params.append('sort', filter.sort);
    return api.get(`/products?${params.toString()}`);
  }

  static async getDetail(id: string): Promise<ApiResponse<IProductDetailDto>> {
    return api.get(`/products/${encodeURIComponent(id)}`);
  }
}
