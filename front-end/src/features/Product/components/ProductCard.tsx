import React from 'react';
import { Link } from 'react-router-dom';
import type { IProductView } from '../types';
import { formatCurrency } from '@shared/utils';
import './ProductCard.scss';

interface ProductCardProps {
  product: IProductView;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  return (
    <Link to={`/products/${product.id}`} className="product-card">
      <div className="product-card__image">
        <img src={product.thumbnail} alt={product.name} loading="lazy" />
      </div>
      <div className="product-card__info">
        <span className="product-card__category">{product.categoryName}</span>
        <h3 className="product-card__name">{product.name}</h3>
        <p className="product-card__price">{formatCurrency(product.minPrice)}</p>
      </div>
    </Link>
  );
};
