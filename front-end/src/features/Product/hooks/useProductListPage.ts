import { useState, useCallback, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import type { IProductView, IProductFilter } from '../types';
import { ProductService } from '../services';

export const useProductListPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<IProductView[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const currentPage = Number(searchParams.get('page')) || 1;
  const category = searchParams.get('category') || '';
  const sort = searchParams.get('sort') || 'createdAt,desc';

  const fetchProducts = useCallback(async (filter: IProductFilter = {}) => {
    setLoading(true);
    try {
      const res = await ProductService.getList({
        page: filter.page || currentPage,
        size: 12,
        category: filter.category ?? category,
        sort: filter.sort ?? sort,
        minPrice: filter.minPrice,
        maxPrice: filter.maxPrice,
      });
      const data = res.data;
      setProducts(data.content.map((dto) => ({
        id: dto.id,
        name: dto.name,
        minPrice: dto.minPrice,
        categoryName: dto.categoryName,
        categorySlug: dto.categorySlug,
        thumbnail: dto.thumbnail,
      })));
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch {
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, [currentPage, category, sort]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const handlePageChange = (page: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', String(page));
    setSearchParams(params);
  };

  const handleCategoryChange = (slug: string) => {
    const params = new URLSearchParams(searchParams);
    if (slug) {
      params.set('category', slug);
    } else {
      params.delete('category');
    }
    params.set('page', '1');
    setSearchParams(params);
  };

  const handleSortChange = (newSort: string) => {
    const params = new URLSearchParams(searchParams);
    params.set('sort', newSort);
    params.set('page', '1');
    setSearchParams(params);
  };

  return {
    products,
    loading,
    totalPages,
    totalElements,
    currentPage,
    category,
    sort,
    handlePageChange,
    handleCategoryChange,
    handleSortChange,
  };
};
