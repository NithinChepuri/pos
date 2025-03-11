export interface Product {
  id: number;
  name: string;
  barcode: string;
  mrp: number;
  clientId: number;
  client?: {
    id: number;
    name: string;
  };
} 