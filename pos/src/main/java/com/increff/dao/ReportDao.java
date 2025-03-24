package com.increff.dao;

import com.increff.model.SalesReportData;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class ReportDao {

    @PersistenceContext
    private EntityManager em;
    //todo: make query as string
    public List<SalesReportData> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Long clientId) {
        String queryStr = 
            "SELECT NEW com.increff.model.SalesReportData(" +
            "p.barcode, " +        // barcode
            "p.name, " +           // product name
            "SUM(oi.quantity), " + // total quantity
            "SUM(oi.quantity * oi.sellingPrice)) " + // total revenue
            "FROM OrderEntity o, OrderItemEntity oi, ProductEntity p " +
            "WHERE o.id = oi.orderId " +
            "AND oi.productId = p.id " +
            "AND o.status = 'INVOICED' " +
            "AND o.createdAt >= :startDate " +
            "AND o.createdAt < :endDate ";
            
        // Add client filter if clientId is provided
        if (clientId != null) {
            queryStr += "AND p.clientId = :clientId ";
        }
            
        queryStr += "GROUP BY p.barcode, p.name " +
                    "ORDER BY p.name";

        TypedQuery<SalesReportData> query = em.createQuery(queryStr, SalesReportData.class)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate);
            
        // Set clientId parameter if provided
        if (clientId != null) {
            query.setParameter("clientId", clientId);
        }

        return query.getResultList();
    }
} 