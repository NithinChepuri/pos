// package com.increff.dao;

// import com.increff.entity.ProductEntity;
// import com.increff.model.products.ProductSearchForm;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.runners.MockitoJUnitRunner;

// import javax.persistence.EntityManager;
// import javax.persistence.TypedQuery;
// import java.math.BigDecimal;
// import java.util.Arrays;
// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.junit.Assert.*;

// @RunWith(MockitoJUnitRunner.class)
// public class ProductDaoTest {

//     @Mock
//     private EntityManager em;

//     @Mock
//     private TypedQuery<ProductEntity> query;

//     @InjectMocks
//     private ProductDao dao;

//     @Test
//     public void testInsert() {
//         ProductEntity product = createProduct("Test Product", "1234567890");
        
//         dao.insert(product);
        
//         verify(em).persist(product);
//     }

//     @Test
//     public void testSelect() {
//         Long id = 1L;
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(em.find(ProductEntity.class, id)).thenReturn(product);

//         ProductEntity result = dao.select(id);

//         assertNotNull(result);
//         assertEquals("Test Product", result.getName());
//         verify(em).find(ProductEntity.class, id);
//     }

//     @Test
//     public void testSelectAll() {
//         List<ProductEntity> expectedProducts = Arrays.asList(
//             createProduct("Product 1", "1234567890"),
//             createProduct("Product 2", "0987654321")
//         );

//         when(em.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedProducts);

//         List<ProductEntity> results = dao.selectAll(0, 10);

//         assertEquals(2, results.size());
//         verify(query).getResultList();
//     }

//     @Test
//     public void testSelectByBarcode() {
//         String barcode = "1234567890";
//         ProductEntity product = createProduct("Test Product", barcode);
//         List<ProductEntity> products = Arrays.asList(product);

//         when(em.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(query);
//         when(query.setParameter("barcode", barcode)).thenReturn(query);
//         when(query.getResultList()).thenReturn(products);

//         ProductEntity result = dao.selectByBarcode(barcode);

//         assertNotNull(result);
//         assertEquals(barcode, result.getBarcode());
//         verify(query).setParameter("barcode", barcode);
//         verify(query).getResultList();
//     }

//     @Test
//     public void testUpdate() {
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(em.merge(product)).thenReturn(product);

//         ProductEntity result = dao.update(product);

//         assertNotNull(result);
//         verify(em).merge(product);
//     }

//     @Test
//     public void testDelete() {
//         ProductEntity product = createProduct("Test Product", "1234567890");
//         when(em.contains(product)).thenReturn(true);

//         dao.delete(product);

//         verify(em).remove(product);
//     }

//     @Test
//     public void testSearch() {
//         ProductSearchForm form = new ProductSearchForm();
//         form.setName("Test");
//         form.setBarcode("1234");
//         form.setClientId(1L);
//         form.setClientName("Test Client");

//         List<ProductEntity> expectedResults = Arrays.asList(
//             createProduct("Test Product 1", "1234567890"),
//             createProduct("Test Product 2", "0987654321")
//         );

//         when(em.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(query);
//         when(query.setParameter(anyString(), any())).thenReturn(query);
//         when(query.setFirstResult(anyInt())).thenReturn(query);
//         when(query.setMaxResults(anyInt())).thenReturn(query);
//         when(query.getResultList()).thenReturn(expectedResults);

//         List<ProductEntity> results = dao.search(form, 0, 10);

//         assertEquals(2, results.size());
//         verify(query).getResultList();
//     }

//     private ProductEntity createProduct(String name, String barcode) {
//         ProductEntity product = new ProductEntity();
//         product.setName(name);
//         product.setBarcode(barcode);
//         product.setMrp(new BigDecimal("99.99"));
//         product.setClientId(1L);
//         return product;
//     }
// } 