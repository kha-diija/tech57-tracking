import { Component, Input, Output, EventEmitter, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {
  LucideAngularModule,
  LayoutDashboard,
  Building2,
  Briefcase,
  Package,
  PackageMinus,
  PackagePlus,
  Users,
  Bot,
  PackageSearch,
  ClipboardList,
  Settings,
  LogOut,
  PanelLeft,
  Sparkles,
  Route,
  BookOpen
} from 'lucide-angular';
import { UserRole } from '../../models/auth.model';

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
  private router = inject(Router);

  readonly icons = {
    LogOut,
    PanelLeft,
    Sparkles
  };

  private readonly allNavItems: NavItem[] = [

    { label: 'Tableau de bord', route: '/admin/dashboard', icon: LayoutDashboard, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'GESTIONNAIRE_STOCK'] },
    { label: 'Tableau de bord', route: '/client/dashboard', icon: LayoutDashboard, roles: ['OBSERVATEUR'] },
    { label: 'Mes ressources', route: '/observateur/ressources', icon: BookOpen, roles: ['OBSERVATEUR'] },
    { label: 'Établissements', route: '/admin/etablissements', icon: Building2, roles: ['ADMINISTRATEUR' , 'TECHNICIEN'] },
    { label: 'Missions', route: '/admin/missions', icon: Briefcase, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },
    { label: 'Simulateur de trajet', route: '/admin/simulateur-trajet', icon: Route, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },
    { label: 'Stock & Matériel', route: '/stock', icon: Package, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK' , 'TECHNICIEN'] },
    { label: 'Sorties de matériel', route: '/sorties', icon: PackageMinus, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Retours & inspection', route: '/retours', icon: PackagePlus, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Utilisateurs', route: '/users', icon: Users, roles: ['ADMINISTRATEUR'] },
    { label: 'Observateurs', route: '/admin/observateurs', icon: Users, roles: ['ADMINISTRATEUR'] },
    { label: 'Assistant IA', route: '/chat-ia', icon: Bot, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Gestion des ressources', route: '/admin/ressources', icon: PackageSearch, roles: ['ADMINISTRATEUR'] },
    { label: 'Gestion et suivi des interventions', route: '/admin/interventions', icon: ClipboardList, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },

    
    { label: 'Paramètres', route: '/settings', icon: Settings, roles: ['ADMINISTRATEUR', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] }

    // { label: 'Guides & Support', route: '/guides', icon: BookOpen, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
    

  ];

  // Adapter dynamiquement les routes selon le rôle de l'utilisateur
  navItems = computed(() => {
    const role = this.authService.currentUser()?.role;
    if (!role) return [];

    return this.allNavItems
      .filter(item => item.roles.includes(role))
      .map(item => {
        let route = item.route;

        // Adaptation du Tableau de bord
        if (item.label === 'Tableau de bord') {
          if (role === 'TECHNICIEN') {
            route = '/technicien/dashboard';
          } else if (role === 'OBSERVATEUR') {
            route = '/observateur/dashboard';
          } else if (role === 'GESTIONNAIRE_STOCK') {
            route = '/stock/dashboard';
          } else {
            route = '/admin/dashboard';
          }
        }

        // Adaptation des Missions pour le technicien
        if (item.label === 'Missions') {
          if (role === 'TECHNICIEN') {
            route = '/technicien/missions';
          } else {
            route = '/admin/missions';
          }
        }

        return { ...item, route };
      });
  });

  toggle() {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  logout() {
    this.router.navigate(['/login']);
  }
}