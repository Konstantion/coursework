import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent {
  @Input() closeOnClick: boolean;
  @Input() showModal: boolean;
  @Input() width: string;
  @Input() height: string;
  @Output() closeModel = new EventEmitter<void>();

  handleMouseup(): void {
    if (this.closeOnClick) {
      this.showModal = false;
      this.closeModel.emit();
    }
  }

  onClose(): void {
    this.showModal = false;
    this.closeModel.emit();
  }
}
