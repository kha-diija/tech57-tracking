import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// ⚠️ Adaptez ces deux chemins d'import à l'emplacement réel de vos
// composants Sidebar et Navbar dans votre projet (ex: '../../shared/sidebar/sidebar'
// ou '../../features/sidebar/sidebar', selon où ils vivent chez vous).
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { Navbar } from '../../shared/components/navbar/navbar';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, Sidebar, Navbar],
  templateUrl: './app-shell.html',
})
export class AppShell {}