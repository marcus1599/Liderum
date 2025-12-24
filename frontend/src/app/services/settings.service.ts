import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private _maxAbsences = 2;

  get maxAbsences(): number {
    return this._maxAbsences;
  }

  set maxAbsences(value: number) {
    this._maxAbsences = value;
  }
}
