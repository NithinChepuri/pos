import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'inrCurrency',
  standalone: true
})
export class InrCurrencyPipe implements PipeTransform {
  transform(value: number | undefined): string {
    if (value === undefined || value === null) {
      return '₹0.00';
    }
    return `₹${value.toFixed(2)}`;
  }
} 