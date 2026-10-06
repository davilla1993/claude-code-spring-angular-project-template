import { Component, computed, input, output } from '@angular/core';
import { PageResponse } from '../../models/api-response.model';

/** Navigation précédent/suivant pour une PageResponse (pages 0-based côté API, 1-based à l'affichage). */
@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.html',
  styleUrl: './pagination.scss',
})
export class Pagination {
  readonly page = input.required<PageResponse<unknown>>();
  readonly pageChange = output<number>();

  protected readonly hasPrevious = computed(() => this.page().page > 0);
  protected readonly hasNext = computed(() => !this.page().last);
}
