# Requirements Document

## Introduction

This document specifies the requirements for an e-commerce backend system for "Hung Hypebeast", a local fashion brand. The system must handle product catalog management, shopping cart functionality, inventory reservation to prevent overselling, order processing with multiple payment methods, order tracking, and basic administrative functions. The primary focus is on preventing race conditions during checkout, particularly for last-item scenarios, while maintaining a seamless guest shopping experience without mandatory user registration.

## Glossary

- **System**: The e-commerce backend application
- **Guest**: A customer who shops without creating an account
- **Guest Token**: A unique identifier stored in browser localStorage to track guest shopping sessions
- **SKU (Stock Keeping Unit)**: A unique identifier for each product variant (combination of size, color, etc.)
- **Product Variant**: A specific variation of a product (e.g., "Dragon T-Shirt - Size L - Black")
- **Cart**: A collection of items a customer intends to purchase
- **Reservation**: A temporary hold on inventory when a customer enters checkout
- **Available Stock**: Physical stock minus active reservations (PhysicalStock - ActiveReservations)
- **Checkout**: The process where a customer provides shipping information and payment details
- **Tracking Token**: A unique UUID used to access order status without authentication
- **COD (Cash on Delivery)**: Payment method where customer pays upon receiving goods
- **SePay**: Third-party payment gateway for bank transfers
- **Pessimistic Locking**: Concurrency control using database row locks (SELECT FOR UPDATE) to prevent conflicting updates
- **Admin User**: Staff member with access to order management functions

## Requirements

### Requirement 1

**User Story:** As a guest, I want to browse products with filtering and pagination, so that I can quickly find items I'm interested in purchasing.

#### Acceptance Criteria

1. WHEN a guest requests the product catalog THEN the System SHALL return a maximum of 20 products per page
2. WHEN a guest specifies a page number THEN the System SHALL return products for that specific page with pagination metadata
3. WHEN a guest applies a price range filter THEN the System SHALL return only products within the specified minimum and maximum price bounds
4. WHEN a guest applies a category filter THEN the System SHALL return only products belonging to the specified category
5. WHEN a guest requests product details THEN the System SHALL return all product variants with their SKU codes, sizes, colors, and current stock quantities

### Requirement 2

**User Story:** As a guest, I want to view detailed information about product variants, so that I can choose the specific size and color I need.

#### Acceptance Criteria

1. WHEN a guest requests a specific product THEN the System SHALL return all available variants with their attributes (size, color, SKU code)
2. WHEN a guest views a product variant THEN the System SHALL display the current available stock quantity
3. WHEN a guest views a product THEN the System SHALL display all associated product images with thumbnail indicators
4. WHEN a product has a category hierarchy THEN the System SHALL display the complete category path

### Requirement 3

**User Story:** As a guest, I want to add items to my shopping cart and have it persist across sessions, so that I can continue shopping later without losing my selections.

#### Acceptance Criteria

1. WHEN a guest adds an item to cart without a Guest Token THEN the System SHALL generate a unique Guest Token and create a new cart
2. WHEN a guest adds an item to cart with an existing Guest Token THEN the System SHALL add the item to the existing cart associated with that token
3. WHEN a guest adds a product variant to cart THEN the System SHALL store the variant ID and requested quantity
4. WHEN a guest modifies cart item quantity THEN the System SHALL update the quantity for that specific cart item
5. WHEN a guest removes an item from cart THEN the System SHALL delete that cart item from the database

### Requirement 4

**User Story:** As a guest, I want the system to validate stock availability when I modify my cart, so that I don't add more items than are available.

#### Acceptance Criteria

1. WHEN a guest adds items to cart THEN the System SHALL verify that the requested quantity does not exceed available stock
2. WHEN available stock is insufficient THEN the System SHALL reject the cart addition and return the maximum available quantity
3. WHEN a guest views their cart THEN the System SHALL display current available stock for each cart item
4. WHEN a guest increases cart item quantity THEN the System SHALL validate against current available stock before updating

### Requirement 5

**User Story:** As a guest, I want the system to reserve inventory when I begin checkout, so that the items remain available for me to complete my purchase.

#### Acceptance Criteria

1. WHEN a guest initiates checkout THEN the System SHALL create inventory reservations for all cart items with a 15-minute expiration time
2. WHEN creating a reservation THEN the System SHALL verify that available stock (PhysicalStock minus ActiveReservations) is sufficient
3. WHEN available stock is insufficient for any cart item THEN the System SHALL reject the entire checkout and return specific items that are unavailable
4. WHEN a reservation is created THEN the System SHALL store the session ID, variant ID, quantity, and expiration timestamp
5. WHEN calculating available stock THEN the System SHALL subtract all active (non-expired) reservation quantities from physical stock

### Requirement 6

**User Story:** As a guest, I want my inventory reservation to expire automatically if I don't complete checkout, so that other customers can purchase those items.

#### Acceptance Criteria

1. WHEN 15 minutes have elapsed since reservation creation THEN the System SHALL mark the reservation status as EXPIRED
2. WHEN a reservation expires THEN the System SHALL make the reserved quantity available for other customers
3. WHEN calculating available stock THEN the System SHALL exclude expired reservations from the calculation
4. WHEN a guest completes checkout THEN the System SHALL mark their reservations as COMPLETED

### Requirement 7

**User Story:** As a guest, I want to prevent race conditions when purchasing the last item in stock, so that only one customer can successfully purchase it.

#### Acceptance Criteria

1. WHEN multiple guests simultaneously attempt to reserve the same last item THEN the System SHALL use pessimistic locking with row-level locks to ensure only one reservation succeeds
2. WHEN a reservation attempt acquires a lock THEN the System SHALL hold the lock until the transaction completes
3. WHEN a second transaction attempts to reserve the same item THEN the System SHALL wait for the first transaction to complete before proceeding
4. WHEN the first transaction completes successfully THEN the System SHALL release the lock and allow the second transaction to see the updated stock quantity

### Requirement 8

**User Story:** As a guest, I want to provide shipping information and select a payment method during checkout, so that I can complete my order.

#### Acceptance Criteria

1. WHEN a guest submits checkout information THEN the System SHALL require customer name, phone number, email, and shipping address
2. WHEN a guest selects COD payment method THEN the System SHALL create an order with status PENDING
3. WHEN a guest selects bank transfer payment method THEN the System SHALL create an order with status PENDING and generate payment instructions
4. WHEN an order is created THEN the System SHALL generate a unique tracking number (UUID format)
5. WHEN an order is created THEN the System SHALL convert active reservations to completed status and deduct physical stock quantities

### Requirement 9

**User Story:** As a guest, I want to receive an order confirmation email with a tracking link, so that I can monitor my order status without creating an account.

#### Acceptance Criteria

1. WHEN an order is successfully created THEN the System SHALL send an email to the customer's provided email address
2. WHEN sending confirmation email THEN the System SHALL include the order tracking number and a public tracking URL
3. WHEN a guest accesses the tracking URL THEN the System SHALL display order details without requiring authentication
4. WHEN displaying order details THEN the System SHALL show current order status, items ordered, quantities, and total amount

### Requirement 10

**User Story:** As a guest, I want to track my order status using only the tracking link, so that I can check delivery progress without logging in.

#### Acceptance Criteria

1. WHEN a guest provides a valid tracking number THEN the System SHALL return the complete order information
2. WHEN a guest provides an invalid tracking number THEN the System SHALL return an error message without exposing other order data
3. WHEN displaying order status THEN the System SHALL show one of the following states: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
4. WHEN an order status changes THEN the System SHALL reflect the updated status immediately on the tracking page

### Requirement 11

**User Story:** As an admin user, I want to view all orders in the system, so that I can manage fulfillment operations.

#### Acceptance Criteria

1. WHEN an admin user requests the order list THEN the System SHALL return all orders with pagination support
2. WHEN displaying orders THEN the System SHALL include tracking number, customer information, total amount, payment method, and current status
3. WHEN an admin user filters by order status THEN the System SHALL return only orders matching that status
4. WHEN an admin user searches by tracking number THEN the System SHALL return the matching order if it exists

### Requirement 12

**User Story:** As an admin user, I want to update order status, so that I can reflect the current fulfillment stage.

#### Acceptance Criteria

1. WHEN an admin user updates order status THEN the System SHALL validate the status transition is allowed
2. WHEN changing status from PENDING to CONFIRMED THEN the System SHALL update the order and record the timestamp
3. WHEN changing status to CANCELLED THEN the System SHALL restore the reserved inventory quantities to available stock
4. WHEN an order status is updated THEN the System SHALL ensure data consistency during concurrent updates
5. WHEN a status update fails due to concurrent modification THEN the System SHALL return an error indicating the order was modified by another user

### Requirement 13

**User Story:** As an admin user, I want the system to automatically update order status when payment is confirmed via SePay webhook, so that I don't need to manually verify bank transfers.

#### Acceptance Criteria

1. WHEN the System receives a valid SePay webhook notification THEN the System SHALL verify the webhook signature for authenticity
2. WHEN a payment is confirmed via webhook THEN the System SHALL locate the order by transaction code
3. WHEN a matching order is found THEN the System SHALL update the order status to CONFIRMED and create a payment transaction record
4. WHEN the webhook contains payment amount THEN the System SHALL verify it matches the order total amount
5. IF payment amount does not match order total THEN the System SHALL log the discrepancy and mark the transaction for manual review

### Requirement 14

**User Story:** As the system, I want to maintain data consistency during concurrent operations, so that inventory and order data remain accurate.

#### Acceptance Criteria

1. WHEN multiple requests attempt to modify the same product variant stock THEN the System SHALL use pessimistic locking to ensure only one modification proceeds at a time
2. WHEN multiple requests attempt to modify the same order THEN the System SHALL ensure data consistency during concurrent updates
3. WHEN a database constraint violation occurs THEN the System SHALL rollback the transaction and return a descriptive error message
4. WHEN creating order items THEN the System SHALL record the price at the time of purchase to maintain historical accuracy

### Requirement 15

**User Story:** As a guest, I want my cart to remain active across browser sessions, so that I can return later to complete my purchase.

#### Acceptance Criteria

1. WHEN a guest closes their browser and returns later with the same Guest Token THEN the System SHALL retrieve their existing cart
2. WHEN a cart has not been accessed for 30 days THEN the System SHALL mark the cart status as ABANDONED
3. WHEN a guest completes checkout THEN the System SHALL mark their cart status as CONVERTED
4. WHEN a guest accesses their cart THEN the System SHALL update the last_active_at timestamp

### Requirement 16

**User Story:** As the system, I want to clean up expired reservations periodically, so that inventory remains available for new customers.

#### Acceptance Criteria

1. WHEN a scheduled cleanup job runs THEN the System SHALL identify all reservations with created_at timestamp older than 15 minutes and status ACTIVE
2. WHEN expired reservations are identified THEN the System SHALL update their status to EXPIRED
3. WHEN the cleanup job completes THEN the System SHALL log the number of reservations expired
4. WHEN calculating available stock after cleanup THEN the System SHALL exclude newly expired reservations
