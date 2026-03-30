import React from 'react';
import { useProductListPage } from '../hooks';
import { ProductCard, Pagination } from '../components';
import './ProductListPage.scss';

export const ProductListPage: React.FC = () => {
  const {
    products,
    loading,
    totalPages,
    totalElements,
    currentPage,
    sort,
    handlePageChange,
    handleSortChange,
  } = useProductListPage();

  return (
    <div className="product-list-page container">
      <div className="product-list-page__header">
        <h1>Sản phẩm ({totalElements})</h1>
        <div className="product-list-page__sort">
          <label>Sắp xếp: </label>
          <select value={sort} onChange={(e) => handleSortChange(e.target.value)}>
            <option value="createdAt,desc">Mới nhất</option>
            <option value="minPrice,asc">Giá: Thấp → Cao</option>
            <option value="minPrice,desc">Giá: Cao → Thấp</option>
            <option value="name,asc">Tên: A → Z</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="loading-spinner" />
      ) : products.length === 0 ? (
        <div className="product-list-page__empty">
          <p>Không tìm thấy sản phẩm nào.</p>
        </div>
      ) : (
        <>
          <div className="product-list-page__grid">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
};
