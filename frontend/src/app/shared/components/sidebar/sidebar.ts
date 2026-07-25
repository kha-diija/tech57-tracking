import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
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
  Sparkles
} from 'lucide-angular';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss' // Si ça ne s'applique pas, remplace par styleUrls: ['./sidebar.scss']
})
export class Sidebar {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  constructor(private router: Router) {}

  readonly icons = {
    LogOut,
    PanelLeft,
    Sparkles
  };

  navItems = [
    { label: 'Tableau de bord', route: '/dashboard', icon: LayoutDashboard },
    { label: 'Établissements', route: '/etablissements', icon: Building2 },
    { label: 'Missions', route: '/missions', icon: Briefcase },
    { label: 'Stock & Matériel', route: '/stock', icon: Package },
    { label: 'Utilisateurs', route: '/users', icon: Users },
    { label: 'Assistant IA', route: '/chat-ia', icon: Bot },
    { label: 'Guides & Support', route: '/guides', icon: BookOpen },
    { label: 'Paramètres', route: '/settings', icon: Settings }
  ];

  toggle() {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  logout() {
    this.router.navigate(['/login']);
  }
}