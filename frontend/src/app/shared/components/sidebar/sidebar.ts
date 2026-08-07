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

  readonly icons = {
    LogOut,
    PanelLeft,
    Sparkles
  };

  private readonly allNavItems: NavItem[] = [
    { label: 'Tableau de bord', route: '/admin/dashboard', icon: LayoutDashboard, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Établissements', route: '/admin/etablissements', icon: Building2, roles: ['ADMINISTRATEUR'] },
    { label: 'Missions', route: '/admin/missions', icon: Briefcase, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },
    { label: 'Simulateur de trajet', route: '/admin/simulateur-trajet', icon: Route, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },
    { label: 'Stock & Matériel', route: '/stock', icon: Package, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Sorties de matériel', route: '/sorties', icon: PackageMinus, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Retours & inspection', route: '/retours', icon: PackagePlus, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Utilisateurs', route: '/users', icon: Users, roles: ['ADMINISTRATEUR'] },
    { label: 'Assistant IA', route: '/chat-ia', icon: Bot, roles: ['ADMINISTRATEUR', 'TECHNICIEN', 'OBSERVATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Gestion des ressources', route: '/admin/ressources', icon: PackageSearch, roles: ['ADMINISTRATEUR', 'GESTIONNAIRE_STOCK'] },
    { label: 'Gestion et suivi des interventions', route: '/admin/interventions', icon: ClipboardList, roles: ['ADMINISTRATEUR', 'TECHNICIEN'] },
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