package fourty3.grocerypos.service;

import fourty3.grocerypos.model.Product;
import fourty3.grocerypos.repository.ProductRepository;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository = new ProductRepository();

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchByName(keyword);
    }

    public List<Product> getActiveProducts() {
        return productRepository.findAllActive();
    }

    public List<Product> searchActiveProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAllActive();
        }
        return productRepository.searchActiveByName(keyword);
    }

    public void addProduct(Product product) {
        validateProduct(product);

        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại.");
        }

        productRepository.insert(product);
    }

    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID is required for update.");
        }

        validateProduct(product);

        if (productRepository.existsByNameExcludingId(product.getName(), product.getId())) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại.");
        }

        productRepository.update(product);
    }

    public boolean deleteProduct(int id) {
        if (productRepository.hasUsageHistory(id)) {
            productRepository.deactivateById(id);
            return false;
        }

        productRepository.deleteById(id);
        return true;
    }

    public void reactivateProduct(int id) {
        productRepository.reactivateById(id);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }

        product.setName(product.getName().trim());

        if (product.getSellingPrice() < 0) {
            throw new IllegalArgumentException("Giá bán không hợp lệ.");
        }
        if (product.getImportPrice() < 0) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ.");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Tồn kho không hợp lệ.");
        }
    }
}