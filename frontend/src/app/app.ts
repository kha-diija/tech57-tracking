import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet], // ← Sidebar et Navbar supprimés
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}