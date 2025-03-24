export interface DailySalesData {
  id?: number;
  date: string | Date;
  formattedDate?: string;
  totalOrders: number;
  totalItems: number;
  totalRevenue: number;
  invoicedOrderCount: number;
  invoicedItemCount: number;
} 