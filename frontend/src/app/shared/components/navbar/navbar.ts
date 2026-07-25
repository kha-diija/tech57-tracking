import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { filter } from 'rxjs/operators';
import { 
  LucideAngularModule, 
  HelpCircle, 
  Bell, 
  Sun, 
  Moon, 
  LogOut, 
  User, 
  ChevronDown 
} from 'lucide-angular';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class Navbar implements OnInit {
  breadcrumbTitle = 'Tableau de bord';
  isDarkMode = true;
  showUserMenu = false;

  userName = 'EL AZZOUZI AYA';
  userRole = 'Administrateur';

  readonly icons = { HelpCircle, Bell, Sun, Moon, LogOut, User, ChevronDown };

  // Dictionnaire des titres selon l'URL
  private routeTitles: { [key: string]: string } = {
    '/dashboard': 'Tableau de bord',
    '/etablissements': 'Établissements',
    '/missions': 'Missions',
    '/stock': 'Stock & Matériel',
    '/users': 'Utilisateurs',
    '/chat-ia': 'Assistant IA',
    '/guides': 'Guides & Support',
    '/settings': 'Paramètres'
  };

  constructor(private router: Router) {}

  ngOnInit() {
    document.documentElement.classList.add('dark');
    
    // Détecte le changement de route pour mettre à jour le fil d'Ariane
    this.updateBreadcrumb(this.router.url);
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updateBreadcrumb(event.urlAfterRedirects || event.url);
      });
  }

  private updateBreadcrumb(url: string) {
    const cleanUrl = url.split('?')[0]; // Nettoie les paramètres d'URL si besoin
    this.breadcrumbTitle = this.routeTitles[cleanUrl] || 'Page';
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.remove('light');
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
      document.documentElement.classList.add('light');
    }
  }

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
  }

  logout() {
    this.router.navigate(['/login']);
  }
}