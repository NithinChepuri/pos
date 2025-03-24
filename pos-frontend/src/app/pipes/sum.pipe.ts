import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sum',
  standalone: true
})
export class SumPipe implements PipeTransform {
  transform(items: any[], prop: string): number {
    if (!items || !items.length) {
      return 0;
    }
    
    return items.reduce((a, b) => a + (b[prop] || 0), 0);
  }
} 