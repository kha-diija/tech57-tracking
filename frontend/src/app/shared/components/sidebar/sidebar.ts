import { Component, Input, Output, EventEmitter, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service'; // ⚠️ adapte selon le chemin réel depuis sidebar/
import {
  LucideAngularModule,
  LayoutDashboard,
  Building2,
  Briefcase,
  Package,
  Users,
  Bot,
  BookOpen,
  Settings,
  LogOut,
  PanelLeft,
  Sparkles,
   Route
} from 'lucide-angular';
import { UserRole } from '../../models/auth.model'; // ⚠️ adapte selon le chemin réel

interface NavItem {
  label: string;
  route: string;
  icon: any;
  roles: UserRole[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  private authService = inject(AuthService);

  readonly icons = {
    LogOut,
    PanelLeft,
    Sparkles
  };

 private readonly allNavItems: NavItem[] = [
  { label: 'Tableau de bord', route: '/dashboard', icon: LayoutDashboard, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
  { label: 'Établissements', route: '/etablissements', icon: Building2, roles: ['ADMINISTRATEUR'] },
  { label: 'Missions', route: '/missions', icon: Briefcase, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },
  { label: 'Simulateur de trajet', route: '/simulateur-trajet', icon: Route, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] }, // 👈 ajout
  { label: 'Stock & Matériel', route: '/stock', icon: Package, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
  { label: 'Utilisateurs', route: '/users', icon: Users, roles: ['ADMINISTRATEUR'] },
  { label: 'Assistant IA', route: '/chat-ia', icon: Bot, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
  { label: 'Guides & Support', route: '/guides', icon: BookOpen, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
  { label: 'Paramètres', route: '/settings', icon: Settings, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] }
];

  navItems = computed(() => {
    const role = this.authService.currentUser()?.role;
    if (!role) return [];
    return this.allNavItems.filter(item => item.roles.includes(role));
  });

  constructor(private router: Router) {}

  toggle() {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  logout() {
    this.router.navigate(['/login']);
  }
}