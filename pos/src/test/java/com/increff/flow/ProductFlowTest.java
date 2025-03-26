// package com.increff.flow;

// import com.increff.entity.ProductEntity;
// import com.increff.model.products.ProductData;
// import com.increff.model.products.ProductForm;
// import com.increff.model.products.ProductSearchForm;
// import com.increff.model.products.UploadResult;
// import com.increff.service.ApiException;
// import com.increff.service.ClientService;
// import com.increff.service.ProductService;
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
// public class ProductFlowTest {

//     @Mock
//     private ProductService productService;

//     @Mock
//     private ClientService clientService;

//     @InjectMocks
//     private ProductFlow flow;

//     @Test
//     public void testAdd() throws ApiException {
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(productService.getByBarcode("1234567890")).thenReturn(null);
//         when(clientService.exists(1L)).thenReturn(true);
//         when(productService.addProduct(product)).thenReturn(convertToData(product));

//         ProductData result = flow.add(product);

//         assertNotNull(result);
//         assertEquals("Test Product", result.getName());
//     }

//     @Test(expected = ApiException.class)
//     public void testAddDuplicateBarcode() throws ApiException {
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(productService.getByBarcode("1234567890")).thenReturn(product);

//         flow.add(product);
//     }

//     @Test(expected = ApiException.class)
//     public void testAddInvalidClient() throws ApiException {
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(productService.getByBarcode("1234567890")).thenReturn(null);
//         when(clientService.exists(1L)).thenReturn(false);

//         flow.add(product);
//     }

//     @Test
//     public void testUpdate() throws ApiException {
//         Long id = 1L;
//         ProductEntity existing = createProduct("Original", "1234567890");
//         ProductEntity updated = createProduct("Updated", "0987654321");
        
//         when(productService.get(id)).thenReturn(existing);
//         when(clientService.exists(1L)).thenReturn(true);
//         when(productService.updateProduct(existing, updated)).thenReturn(convertToData(updated));

//         ProductData result = flow.update(id, updated);

//         assertNotNull(result);
//         assertEquals("Updated", result.getName());
//     }

//     @Test(expected = ApiException.class)
//     public void testUpdateNonExistentProduct() throws ApiException {
//         Long id = 1L;
//         ProductEntity updated = createProduct("Updated", "0987654321");
//         when(productService.get(id)).thenReturn(null);

//         flow.update(id, updated);
//     }

//     @Test
//     public void testUploadProducts() throws ApiException {
//         List<ProductEntity> products = Arrays.asList(
//             createProduct("Product 1", "1234567890"),
//             createProduct("Product 2", "0987654321")
//         );
//         List<ProductForm> forms = Arrays.asList(
//             createProductForm("Product 1", "1234567890"),
//             createProductForm("Product 2", "0987654321")
//         );

//         when(productService.getByBarcode(anyString())).thenReturn(null);
//         when(clientService.exists(anyLong())).thenReturn(true);
//         when(productService.addProduct(any())).thenAnswer(i -> convertToData((ProductEntity)i.getArguments()[0]));

//         UploadResult<ProductData> result = flow.uploadProducts(products, forms);

//         assertEquals(2, result.getSuccessCount());
//         assertEquals(0, result.getErrorCount());
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

//     private ProductForm createProductForm(String name, String barcode) {
//         ProductForm form = new ProductForm();
//         form.setName(name);
//         form.setBarcode(barcode);
//         form.setMrp(new BigDecimal("99.99"));
//         form.setClientId(1L);
//         return form;
//     }

//     private ProductData convertToData(ProductEntity product) {
//         ProductData data = new ProductData();
//         data.setId(product.getId());
//         data.setName(product.getName());
//         data.setBarcode(product.getBarcode());
//         data.setMrp(product.getMrp());
//         data.setClientId(product.getClientId());
//         return data;
//     }
// } 