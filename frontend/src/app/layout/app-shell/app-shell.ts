import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { Navbar } from '../../shared/components/navbar/navbar';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, Sidebar, Navbar],
  templateUrl: './app-shell.html',
  styleUrl: '../../app.scss'
})
export class AppShell {
  collapsed = signal(false);
}