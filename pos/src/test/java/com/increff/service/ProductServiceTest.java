// package com.increff.service;

// import com.increff.dao.ProductDao;
// import com.increff.entity.ProductEntity;
// import com.increff.model.products.ProductData;
// import com.increff.model.products.ProductSearchForm;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;

// import java.math.BigDecimal;
// import java.util.Arrays;
// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class ProductServiceTest {

//     @Mock
//     private ProductDao dao;

//     @Mock
//     private OrderItemService orderItemService;

//     @Mock
//     private InventoryService inventoryService;

//     @InjectMocks
//     private ProductService service;

//     @Before
//     public void setUp() {
//         when(orderItemService.existsByProductId(anyLong())).thenReturn(false);
//         when(inventoryService.existsByProductId(anyLong())).thenReturn(false);
//     }

//     @Test
//     public void testAdd() {
//         ProductEntity product = createProduct("Test Product", "1234567890");
        
//         service.add(product);
        
//         verify(dao).insert(product);
//     }

//     @Test
//     public void testAddProduct() {
//         ProductEntity product = createProduct("Test Product", "1234567890");
        
//         ProductData result = service.addProduct(product);
        
//         assertNotNull(result);
//         assertEquals("Test Product", result.getName());
//         verify(dao).insert(product);
//     }

//     @Test
//     public void testGet() {
//         Long id = 1L;
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(dao.select(id)).thenReturn(product);

//         ProductEntity result = service.get(id);

//         assertNotNull(result);
//         assertEquals("Test Product", result.getName());
//     }

//     @Test
//     public void testGetProductData() throws ApiException {
//         Long id = 1L;
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(dao.select(id)).thenReturn(product);

//         ProductData result = service.getProductData(id);

//         assertNotNull(result);
//         assertEquals("Test Product", result.getName());
//     }

//     @Test(expected = ApiException.class)
//     public void testGetProductDataNotFound() throws ApiException {
//         Long id = 1L;
//         when(dao.select(id)).thenReturn(null);

//         service.getProductData(id);
//     }

//     @Test
//     public void testGetAll() {
//         List<ProductEntity> products = Arrays.asList(
//             createProduct("Product 1", "1234567890"),
//             createProduct("Product 2", "0987654321")
//         );
//         when(dao.selectAll(0, 10)).thenReturn(products);

//         List<ProductEntity> results = service.getAll(0, 10);

//         assertEquals(2, results.size());
//     }

//     @Test
//     public void testUpdateProduct() {
//         ProductEntity existing = createProduct("Original", "1234567890");
//         ProductEntity updated = createProduct("Updated", "0987654321");

//         ProductData result = service.updateProduct(existing, updated);

//         assertNotNull(result);
//         assertEquals("Updated", result.getName());
//         assertEquals("0987654321", result.getBarcode());
//     }

//     @Test
//     public void testGetByBarcode() {
//         String barcode = "1234567890";
//         ProductEntity product = createProduct("Test Product", barcode);
//         when(dao.selectByBarcode(barcode)).thenReturn(product);

//         ProductEntity result = service.getByBarcode(barcode);

//         assertNotNull(result);
//         assertEquals(barcode, result.getBarcode());
//     }

//     @Test
//     public void testSearch() {
//         ProductSearchForm form = new ProductSearchForm();
//         List<ProductEntity> products = Arrays.asList(
//             createProduct("Product 1", "1234567890"),
//             createProduct("Product 2", "0987654321")
//         );
//         when(dao.search(form, 0, 10)).thenReturn(products);

//         List<ProductEntity> results = service.search(form, 0, 10);

//         assertEquals(2, results.size());
//     }

    

//     @Test(expected = ApiException.class)
//     public void testDeleteProductNotFound() throws ApiException {
//         Long id = 1L;
//         when(dao.select(id)).thenReturn(null);

//         service.deleteProduct(id);
//     }

//     @Test
//     public void testSearchProducts() {
//         // Arrange
//         ProductSearchForm form = new ProductSearchForm();
//         form.setName("Test");
//         form.setBarcode("123");
        
//         List<ProductEntity> expectedProducts = Arrays.asList(
//             createProduct("Test Product", "1234567890"),
//             createProduct("Test Item", "1234567891")
//         );
        
//         when(dao.search(form, 0, 10)).thenReturn(expectedProducts);
        
//         // Act
//         List<ProductData> results = service.searchProductData(form, 0, 10);
        
//         // Assert
//         assertEquals(2, results.size());
//         verify(dao).search(form, 0, 10);
//     }

//     @Test
//     public void testGetAllProductData() {
//         // Arrange
//         List<ProductEntity> products = Arrays.asList(
//             createProduct("Product 1", "1234567890"),
//             createProduct("Product 2", "0987654321")
//         );
//         when(dao.selectAll(0, 10)).thenReturn(products);
        
//         // Act
//         List<ProductData> results = service.getAllProductData(0, 10);
        
//         // Assert
//         assertEquals(2, results.size());
//         verify(dao).selectAll(0, 10);
//     }

//     private ProductEntity createProduct(String name, String barcode) {
//         ProductEntity product = new ProductEntity();
//         product.setId(1L);
//         product.setName(name);
//         product.setBarcode(barcode);
//         product.setMrp(new BigDecimal("99.99"));
//         product.setClientId(1L);
//         return product;
//     }
// } 