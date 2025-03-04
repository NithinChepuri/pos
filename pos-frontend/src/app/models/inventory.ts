export interface Inventory {
  id: number;
  productId: number;
  quantity: number;
  product?: {
    name: string;
    barcode: string;
  };
} 