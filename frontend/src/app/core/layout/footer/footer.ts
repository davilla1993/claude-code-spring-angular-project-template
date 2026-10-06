import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { APP_NAME } from '../../../shared/constants/app.constants';

@Component({
  selector: 'app-footer',
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  protected readonly appName = APP_NAME;
  protected readonly year = new Date().getFullYear();
}
