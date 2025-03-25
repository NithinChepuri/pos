package com.increff.dao;

import com.increff.entity.ProductEntity;
import com.increff.model.products.ProductSearchForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductDaoTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductEntity> query;

    @InjectMocks
    private ProductDao dao;

    @Before
    public void setUp() {
        when(query.setParameter(anyString(), any())).thenReturn(query);
    }

    @Test
    public void testInsert() {
        ProductEntity product = createProduct(null, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        
        dao.insert(product);
        
        verify(em).persist(product);
    }

    @Test
    public void testSelect() {
        Long id = 1L;
        ProductEntity product = createProduct(id, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        when(em.find(ProductEntity.class, id)).thenReturn(product);
        
        ProductEntity result = dao.select(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(em).find(ProductEntity.class, id);
    }

    @Test
    public void testSelectAll() {
        List<ProductEntity> expectedProducts = Arrays.asList(
            createProduct(1L, "Product 1", "SKU1", new BigDecimal("100.00")),
            createProduct(2L, "Product 2", "SKU2", new BigDecimal("200.00"))
        );

        when(em.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedProducts);
        
        List<ProductEntity> results = dao.selectAll(0, 10);
        
        assertEquals(2, results.size());
        verify(query).getResultList();
    }

    @Test
    public void testSelectByBarcode() {
        String barcode = "TESTSKU";
        ProductEntity product = createProduct(1L, "Test Product", barcode, new BigDecimal("100.00"));
        List<ProductEntity> products = Arrays.asList(product);
        
        when(em.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(products);
        
        ProductEntity result = dao.selectByBarcode(barcode);
        
        assertNotNull(result);
        assertEquals(barcode, result.getBarcode());
        verify(query).setParameter("barcode", barcode);
        verify(query).getResultList();
    }

    @Test
    public void testUpdate() {
        ProductEntity product = createProduct(1L, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        
        dao.update(product);
        
        verify(em).merge(product);
    }

    @Test
    public void testDelete() {
        ProductEntity product = createProduct(1L, "Test Product", "TESTSKU", new BigDecimal("100.00"));
        when(em.merge(product)).thenReturn(product);
        
        dao.delete(product);
        
        verify(em).remove(any(ProductEntity.class));
    }

    @Test
    public void testSearch() {
        ProductSearchForm form = new ProductSearchForm();
        form.setName("Test");
        
        List<ProductEntity> expectedResults = Arrays.asList(
            createProduct(1L, "Test Product 1", "SKU1", new BigDecimal("100.00")),
            createProduct(2L, "Test Product 2", "SKU2", new BigDecimal("200.00"))
        );

        when(em.createQuery(anyString(), eq(ProductEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedResults);
        
        List<ProductEntity> results = dao.search(form, 0, 10);
        
        assertEquals(2, results.size());
        verify(query).getResultList();
    }

    private ProductEntity createProduct(Long id, String name, String barcode, BigDecimal mrp) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setMrp(mrp);
        return product;
    }
} 