Table cart {
  id uuid [pk]
  guest_token varchar [unique]
  user_id uuid
  status varchar // CartStatus enum: ACTIVE, ABANDONED, CONVERTED
  last_active_at timestamp
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table cart_items {
  id uuid [pk]
  cart_id uuid [not null, ref: > cart.id]
  variant_id uuid [not null, ref: > product_variants.id]
  quantity integer
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table category {
  id uuid [pk]
  name varchar [not null]
  slug varchar [unique, not null]
  parent_id uuid [ref: > category.id]
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table products {
  id uuid [pk]
  name varchar [not null]
  description text
  base_price decimal
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table product_variants {
  id uuid [pk]
  product_id uuid [not null, ref: > products.id]
  sku_code varchar [unique, not null]
  size varchar
  color varchar
  stock_quantity integer [not null]
  version integer
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table product_images {
  id uuid [pk]
  product_id uuid [not null, ref: > products.id]
  image_url varchar [not null]
  is_thumbnail boolean
  display_order integer
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table orders {
  id uuid [pk]
  tracking_number varchar [unique]
  customer_name varchar
  customer_phone varchar
  customer_email varchar
  shipping_address text
  total_amount decimal
  payment_method varchar // PaymentMethod enum: CASH_ON_DELIVERY, BANK_TRANSFER, CREDIT_CARD, E_WALLET
  status varchar // OrderStatus enum: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
  version integer
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table order_items {
  id uuid [pk]
  order_id uuid [not null, ref: > orders.id]
  variant_id uuid [not null, ref: > product_variants.id]
  quantity integer
  price_at_purchase decimal
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table payment_transactions {
  id uuid [pk]
  order_id uuid [not null, ref: > orders.id]
  transaction_code varchar [unique]
  amount decimal
  payment_method varchar // PaymentMethod enum
  transaction_content text
  status varchar
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table inventory_reservations {
  id uuid [pk]
  variant_id uuid [not null, ref: > product_variants.id]
  quantity integer [not null]
  session_id varchar [not null]
  status varchar // ReservationStatus enum: ACTIVE, EXPIRED, COMPLETED, CANCELLED
  created_at timestamp [not null]
  updated_at timestamp [not null]
}