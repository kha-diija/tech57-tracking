import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GsUser, GsUserRole } from '../../shared/models/gsuser.model';
import { GsUserService } from '../../shared/services/gsuser.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class Users implements OnInit {
  private gsUserService = inject(GsUserService);
  private fb = inject(FormBuilder);

  users = signal<GsUser[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string>('');

  searchQuery = signal<string>('');
  roleFilter = signal<string>('ALL');

  filteredUsers = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const role = this.roleFilter();

    return this.users().filter((user) => {
      const matchesName =
        !query ||
        user.firstname?.toLowerCase().includes(query) ||
        user.lastname?.toLowerCase().includes(query) ||
        `${user.firstname} ${user.lastname}`.toLowerCase().includes(query);

      const matchesRole = role === 'ALL' || user.role === role;

      return matchesName && matchesRole;
    });
  });

  isModalOpen = signal<boolean>(false);
  submitting = signal<boolean>(false);
  formError = signal<string>('');
  userForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadUsers();
  }

  initForm(): void {
    this.userForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telephone: [''],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: ['TECHNICIEN' as GsUserRole, Validators.required],
    });
  }

  loadUsers(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.gsUserService.getUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message || 'Erreur lors du chargement des utilisateurs.');
        this.loading.set(false);
      }
    });
  }

  openModal(): void {
    this.userForm.reset({ role: 'TECHNICIEN' });
    this.formError.set('');
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    if (this.submitting()) return;
    this.isModalOpen.set(false);
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.formError.set('');

    this.gsUserService.createGsUser(this.userForm.value).subscribe({
      next: (createdUser) => {
        this.users.update((list) => [...list, createdUser]);
        this.submitting.set(false);
        this.closeModal();
      },
      error: (err) => {
        this.submitting.set(false);
        this.formError.set(err?.error?.message || 'Erreur lors de la création.');
      }
    });
  }

  canManageUser(targetUser: GsUser): boolean {
    return targetUser.role !== 'ADMINISTRATEUR';
  }

  /**
   * Gère le blocage et déblocage de l'utilisateur
   */
  toggleUserStatus(user: GsUser): void {
    if (!this.canManageUser(user)) return;

    const action = user.isActive ? 'bloquer' : 'débloquer';
    if (!confirm(`Voulez-vous ${action} ${user.firstname} ${user.lastname} ?`)) return;

    this.gsUserService.toggleStatus(user.id).subscribe({
      next: (updatedUser) => {
        this.users.update((list) =>
          list.map((u) => (u.id === updatedUser.id ? { ...u, isActive: updatedUser.isActive } : u))
        );
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message || 'Erreur lors du changement de statut.');
      }
    });
  }

  deleteUser(user: GsUser): void {
    if (!this.canManageUser(user)) return;

    if (confirm(`Voulez-vous supprimer définitivement ${user.firstname} ${user.lastname} ?`)) {
      this.gsUserService.deleteGsUser(user.id).subscribe({
        next: () => {
          this.users.update((list) => list.filter((u) => u.id !== user.id));
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message || 'Erreur lors de la suppression.');
        }
      });
    }
  }
}
