import { useState, useCallback, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import type { IProductDetailView, IProductVariant } from '../types';
import { ProductService } from '../services';

export const useProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<IProductDetailView | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedVariant, setSelectedVariant] = useState<IProductVariant | null>(null);
  const [selectedImage, setSelectedImage] = useState(0);
  const [quantity, setQuantity] = useState(1);

  const fetchProduct = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await ProductService.getDetail(id);
      const data = res.data;
      setProduct({
        id: data.id,
        name: data.name,
        description: data.description,
        minPrice: data.minPrice,
        maxPrice: data.maxPrice,
        categoryName: data.categoryName,
        images: data.images,
        variants: data.variants,
      });
      if (data.variants.length > 0) {
        setSelectedVariant(data.variants[0]);
      }
    } catch {
      setProduct(null);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchProduct();
  }, [fetchProduct]);

  const handleVariantSelect = (variant: IProductVariant) => {
    setSelectedVariant(variant);
    setQuantity(1);
  };

  const handleQuantityChange = (newQty: number) => {
    if (!selectedVariant) return;
    const max = selectedVariant.stockQuantity;
    setQuantity(Math.max(1, Math.min(newQty, max)));
  };

  const availableSizes = product
    ? [...new Set(product.variants.map((v) => v.size).filter(Boolean))]
    : [];

  const availableColors = product
    ? [...new Set(product.variants.map((v) => v.color).filter(Boolean))]
    : [];

  return {
    product,
    loading,
    selectedVariant,
    selectedImage,
    quantity,
    availableSizes,
    availableColors,
    setSelectedImage,
    handleVariantSelect,
    handleQuantityChange,
  };
};
