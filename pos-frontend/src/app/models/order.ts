export interface JavaDateTime {
  year: number;
  monthValue: number;
  dayOfMonth: number;
  hour: number;
  minute: number;
  second: number;
  nano: number;
  dayOfWeek: string;
  dayOfYear: number;
  month: string;
  chronology: {
    calendarType: string;
    id: string;
  };
  zone: {
    id: string;
    rules: any;
  };
  offset: {
    totalSeconds: number;
    id: string;
    rules: any;
  };
}

export enum OrderStatus {
  CREATED = 'CREATED',
  INVOICED = 'INVOICED'
}

export interface OrderItem {
  barcode: string;
  productName?: string;
  quantity: number;
  sellingPrice: number;
  total?: number;
}

export interface Order {
  id: number;
  status: OrderStatus;
  createdAt: string | JavaDateTime;
  invoicePath?: string;
  items: OrderItem[];
} 