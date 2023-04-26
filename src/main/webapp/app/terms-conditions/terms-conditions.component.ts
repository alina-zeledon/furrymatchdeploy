import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'jhi-terms-conditions',
  templateUrl: './terms-conditions.component.html',
  styleUrls: ['./terms-conditions.component.scss'],
})
export class TermsConditionsComponent {
  @Output() termsAccepted = new EventEmitter<void>();

  constructor() {}

  accept(): void {
    this.termsAccepted.emit();
  }
}
