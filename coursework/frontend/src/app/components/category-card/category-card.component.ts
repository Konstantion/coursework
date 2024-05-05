import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CategoryDto} from 'src/app/models/dto/category/category-dto';

@Component({
  selector: 'app-category-card',
  templateUrl: './category-card.component.html',
  styleUrls: ['./category-card.component.css']
})
export class CategoryCardComponent {
  @Input() category: CategoryDto;

  @Output() onClick = new EventEmitter<string>();

  constructor() {
  }

  cardClick() {
    this.onClick.emit(this.category.id);
  }
}
