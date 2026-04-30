
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'sum', standalone: true })
export class SumPipe implements PipeTransform {
  transform(arr: number[]): number {
    return arr?.reduce((a, b) => a + b, 0) || 1;
  }
}