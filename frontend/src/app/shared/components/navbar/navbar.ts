import { Component, OnInit, inject } from '@angular/core';
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
  ChevronDown,
  Check,
  CheckCheck
} from 'lucide-angular';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../../shared/services/notification.service'; // ⚠️ ajuste le chemin selon ton arborescence

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class Navbar implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  notificationService = inject(NotificationService);

  breadcrumbTitle = 'Tableau de bord';
  isDarkMode = true;
  showUserMenu = false;
  showNotifPanel = false;

  userName = 'Utilisateur';
  userRole = 'Membre';
  userInitials = 'U';

  readonly icons = { HelpCircle, Bell, Sun, Moon, LogOut, User, ChevronDown, Check, CheckCheck };

  private routeTitles: { [key: string]: string } = {
    '/dashboard': 'Tableau de bord',
    '/admin/dashboard': 'Tableau de bord Administrateur',
    '/technicien/dashboard': 'Tableau de bord Technicien',
    '/client/dashboard': 'Tableau de bord Client',
    '/gestionnaire/dashboard': 'Tableau de bord Gestionnaire',
    '/etablissements': 'Établissements',
    '/missions': 'Missions',
    '/stock': 'Stock & Matériel',
    '/sorties': 'Sorties de matériel',
    '/retours': 'Retours & inspection',
    '/users': 'Utilisateurs',
    '/chat-ia': 'Assistant IA',
    '/guides': 'Guides & Support',
    '/settings': 'Paramètres'
  };

  ngOnInit() {
    document.documentElement.classList.add('dark');
    
    this.loadUserData();
    this.notificationService.initialiser();

    this.updateBreadcrumb(this.router.url);
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updateBreadcrumb(event.urlAfterRedirects || event.url);
      });
  }

  private loadUserData() {
    const user = this.authService.currentUser();

    if (user) {
      const nom = user.nom || '';
      const prenom = user.prenom || '';
      
      this.userName = `${prenom} ${nom}`.trim() || user.email || 'Utilisateur';
      this.userRole = user.role || 'Membre';

      const pInitial = prenom ? prenom.charAt(0).toUpperCase() : '';
      const nInitial = nom ? nom.charAt(0).toUpperCase() : '';
      this.userInitials = (pInitial + nInitial) || this.userName.charAt(0).toUpperCase() || 'U';
    }
  }

  private updateBreadcrumb(url: string) {
    const cleanUrl = url.split('?')[0];
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
    this.showNotifPanel = false;
  }

  toggleNotifPanel() {
    this.showNotifPanel = !this.showNotifPanel;
    this.showUserMenu = false;
  }

  marquerLue(id: number, event: Event) {
    event.stopPropagation();
    this.notificationService.marquerCommeLue(id);
  }

  marquerToutesLues() {
    this.notificationService.marquerToutesCommeLues();
  }

  logout() {
    this.notificationService.deconnecter();
    this.authService.logout();
  }
}