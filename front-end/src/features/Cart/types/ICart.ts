export interface ICartItemDto {
  id: string;
  variantId: string;
  productName: string;
  skuCode: string;
  size: string;
  color: string;
  thumbnail: string;
  unitPrice: number;
  quantity: number;
  subTotal: number;
  maxStock: number;
}

export interface ICartItemView {
  id: string;
  variantId: string;
  productName: string;
  skuCode: string;
  size: string;
  color: string;
  thumbnail: string;
  unitPrice: number;
  quantity: number;
  subTotal: number;
  maxStock: number;
}

export interface ICartDto {
  id: string;
  totalAmount: number;
  totalItems: number;
  items: ICartItemDto[];
}

export interface ICartView {
  id: string;
  totalAmount: number;
  totalItems: number;
  items: ICartItemView[];
}

export interface IAddToCartRequest {
  variantId: string;
  quantity: number;
}

export interface IUpdateCartItemRequest {
  itemId: string;
  quantity: number;
}
