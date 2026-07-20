import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { Navbar } from './shared/components/navbar/navbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Sidebar, Navbar], 
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class AppComponent {
  title = 'frontend';
}