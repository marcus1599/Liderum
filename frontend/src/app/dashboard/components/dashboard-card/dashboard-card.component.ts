import { Component } from '@angular/core';
import { MatCard, MatCardTitle, MatCardContent, MatCardModule } from "@angular/material/card";
import { MatIcon } from "@angular/material/icon";

@Component({
  selector: 'app-dashboard-card',
  imports: [MatCardModule, MatCardTitle, MatIcon, MatCardContent],
  templateUrl: './dashboard-card.component.html',
  styleUrl: './dashboard-card.component.scss'
})
export class DashboardCardComponent {

}
