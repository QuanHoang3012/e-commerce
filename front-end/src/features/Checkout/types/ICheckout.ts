export interface ICheckoutInitiateResponse {
  orderId: string;
  reservedUntil: string;
  totalItems: number;
  totalAmount: number;
}

export interface ICheckoutConfirmRequest {
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  shippingAddress: string;
  paymentMethod: 'COD' | 'BANK_TRANSFER';
  note?: string;
}

export interface ICheckoutConfirmResponse {
  orderId: string;
  totalAmount: number;
  message: string;
}

export interface IOrderTrackingResponse {
  orderId: string;
  trackingNumber: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  shippingAddress: string;
  totalAmount: number;
  paymentMethod: string;
  status: string;
  orderDate: string;
  lastUpdated: string;
  items: IOrderItemResponse[];
  timeline: IOrderTimeline;
}

export interface IOrderItemResponse {
  productName: string;
  variantInfo: string;
  skuCode: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface IOrderTimeline {
  pending: ITimelineStep;
  confirmed: ITimelineStep;
  paid: ITimelineStep;
  processing: ITimelineStep;
  shipping: ITimelineStep;
  delivered: ITimelineStep;
  completed: ITimelineStep;
}

export interface ITimelineStep {
  completed: boolean;
  timestamp: string | null;
  note: string | null;
}
