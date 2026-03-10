import React from 'react';
import { useProductDetailPage } from '../hooks';
import { useCart } from '@features/Cart/hooks';
import { formatCurrency } from '@shared/utils';
import './ProductDetailPage.scss';

export const ProductDetailPage: React.FC = () => {
  const {
    product,
    loading,
    selectedVariant,
    selectedImage,
    quantity,
    setSelectedImage,
    handleVariantSelect,
    handleQuantityChange,
  } = useProductDetailPage();

  const { handleAddToCart } = useCart();

  if (loading) return <div className="loading-spinner" />;
  if (!product) return <div className="container"><p>Không tìm thấy sản phẩm.</p></div>;

  const onAddToCart = () => {
    if (!selectedVariant) return;
    handleAddToCart(selectedVariant.id, quantity);
  };

  return (
    <div className="product-detail container">
      <div className="product-detail__gallery">
        <div className="product-detail__main-image">
          <img src={product.images[selectedImage] || product.images[0]} alt={product.name} />
        </div>
        {product.images.length > 1 && (
          <div className="product-detail__thumbnails">
            {product.images.map((img, idx) => (
              <button
                key={idx}
                className={`product-detail__thumb ${idx === selectedImage ? 'product-detail__thumb--active' : ''}`}
                onClick={() => setSelectedImage(idx)}
              >
                <img src={img} alt={`${product.name} ${idx + 1}`} />
              </button>
            ))}
          </div>
        )}
      </div>

      <div className="product-detail__info">
        <span className="product-detail__category">{product.categoryName}</span>
        <h1 className="product-detail__name">{product.name}</h1>

        <div className="product-detail__price">
          {selectedVariant ? (
            <span className="product-detail__price-current">{formatCurrency(selectedVariant.price)}</span>
          ) : (
            <span className="product-detail__price-current">
              {formatCurrency(product.minPrice)}
              {product.maxPrice > product.minPrice && ` - ${formatCurrency(product.maxPrice)}`}
            </span>
          )}
        </div>

        {product.variants.length > 0 && (
          <div className="product-detail__variants">
            <h3>Phân loại:</h3>
            <div className="product-detail__variant-list">
              {product.variants.map((variant) => (
                <button
                  key={variant.id}
                  className={`product-detail__variant-btn ${selectedVariant?.id === variant.id ? 'product-detail__variant-btn--active' : ''} ${variant.stockQuantity === 0 ? 'product-detail__variant-btn--disabled' : ''}`}
                  onClick={() => handleVariantSelect(variant)}
                  disabled={variant.stockQuantity === 0}
                >
                  {[variant.size, variant.color].filter(Boolean).join(' / ')}
                </button>
              ))}
            </div>
          </div>
        )}

        {selectedVariant && (
          <>
            <div className="product-detail__stock">
              Còn {selectedVariant.stockQuantity} sản phẩm
            </div>

            <div className="product-detail__quantity">
              <h3>Số lượng:</h3>
              <div className="product-detail__quantity-control">
                <button onClick={() => handleQuantityChange(quantity - 1)} disabled={quantity <= 1}>−</button>
                <span>{quantity}</span>
                <button onClick={() => handleQuantityChange(quantity + 1)} disabled={quantity >= selectedVariant.stockQuantity}>+</button>
              </div>
            </div>

            <button className="btn btn-primary btn-lg product-detail__add-cart" onClick={onAddToCart}>
              Thêm vào giỏ hàng
            </button>
          </>
        )}

        {product.description && (
          <div className="product-detail__description">
            <h3>Mô tả sản phẩm</h3>
            <div dangerouslySetInnerHTML={{ __html: product.description }} />
          </div>
        )}
      </div>
    </div>
  );
};
