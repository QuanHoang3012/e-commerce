package org.project.ecommerce.dto.response;

import java.math.BigDecimal;

public interface ProductPriceRange {
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();
}
