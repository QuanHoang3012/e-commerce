export interface IProductDto {
  id: string;
  name: string;
  minPrice: number;
  categoryName: string;
  categorySlug: string;
  thumbnail: string;
}

export interface IProductView {
  id: string;
  name: string;
  minPrice: number;
  categoryName: string;
  categorySlug: string;
  thumbnail: string;
}

export interface IProductVariant {
  id: string;
  skuCode: string;
  size: string;
  color: string;
  price: number;
  stockQuantity: number;
}

export interface IProductDetailDto {
  id: string;
  name: string;
  description: string;
  minPrice: number;
  maxPrice: number;
  categoryName: string;
  images: string[];
  variants: IProductVariant[];
}

export interface IProductDetailView {
  id: string;
  name: string;
  description: string;
  minPrice: number;
  maxPrice: number;
  categoryName: string;
  images: string[];
  variants: IProductVariant[];
}

export interface IProductFilter {
  page?: number;
  size?: number;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: string;
}
