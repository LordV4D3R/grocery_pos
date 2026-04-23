package fourty3.grocerypos.service;

import fourty3.grocerypos.model.Product;
import fourty3.grocerypos.repository.StockImportRepository;

public class InventoryService {

    private final StockImportRepository stockImportRepository = new StockImportRepository();

    public void importStock(Product product, int importQuantity, double newImportPrice) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm.");
        }

        if (importQuantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        }

        if (newImportPrice < 0) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ.");
        }

        stockImportRepository.importStock(product, importQuantity, newImportPrice);
    }
}
