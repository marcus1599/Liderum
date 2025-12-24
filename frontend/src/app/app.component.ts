import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'; 


@Component({
  selector: 'app-root',
  imports: [RouterOutlet,MatProgressSpinnerModule
   
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'liderum-front';
}
