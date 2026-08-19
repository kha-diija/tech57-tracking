import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GsUser, GsUserRole } from '../../shared/models/gsuser.model';
import { GsUserService } from '../../shared/services/gsuser.service';
import { ConfirmationDialogComponent } from '../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { ConfirmationService } from '../../shared/services/confirmation.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ConfirmationDialogComponent],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class Users implements OnInit {
  private gsUserService = inject(GsUserService);
  private fb = inject(FormBuilder);
  private confirmationService = inject(ConfirmationService);

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
  isEditMode = signal<boolean>(false);
  editingUserId = signal<number | null>(null);
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

  /**
   * Ouvre le modal en mode création
   */
  openModal(): void {
    this.isEditMode.set(false);
    this.editingUserId.set(null);
    this.userForm.reset({ role: 'TECHNICIEN' });
    this.formError.set('');
    
    // Réactiver les validateurs pour la création
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(8)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.userForm.get('role')?.enable();
    
    this.isModalOpen.set(true);
  }

  /**
   * Ouvre le modal en mode édition avec les données de l'utilisateur
   */
  openEditModal(user: GsUser): void {
    this.isEditMode.set(true);
    this.editingUserId.set(user.id);
    
    // Pré-remplir le formulaire
    this.userForm.patchValue({
      firstname: user.firstname,
      lastname: user.lastname,
      email: user.email,
      telephone: user.telephone || '',
      role: user.role
    });

    // Désactiver le champ password et le rendre optionnel (car c'est une édition)
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.userForm.get('password')?.setValue('');

    // Désactiver le rôle (on ne change pas le rôle en édition)
    this.userForm.get('role')?.disable();

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

    if (this.isEditMode()) {
      this.handleEditSubmit();
    } else {
      this.handleCreateSubmit();
    }
  }

  /**
   * Soumet la création d'un nouvel utilisateur
   */
  private handleCreateSubmit(): void {
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

  /**
   * Soumet la modification d'un utilisateur existant
   */
  private handleEditSubmit(): void {
    const userId = this.editingUserId();
    if (userId === null) return;

    const updateData = {
      firstname: this.userForm.get('firstname')?.value,
      lastname: this.userForm.get('lastname')?.value,
      email: this.userForm.get('email')?.value,
      telephone: this.userForm.get('telephone')?.value || ''
    };

    this.gsUserService.updateGsUser(userId, updateData).subscribe({
      next: (updatedUser) => {
        this.users.update((list) =>
          list.map((u) => (u.id === updatedUser.id ? updatedUser : u))
        );
        this.submitting.set(false);
        this.closeModal();
      },
      error: (err) => {
        this.submitting.set(false);
        this.formError.set(err?.error?.message || 'Erreur lors de la modification.');
      }
    });
  }

  canManageUser(targetUser: GsUser): boolean {
    return targetUser.role !== 'ADMINISTRATEUR';
  }

  /**
   * Gère le blocage et déblocage de l'utilisateur avec modal de confirmation
   */
  async toggleUserStatus(user: GsUser): Promise<void> {
    if (!this.canManageUser(user)) return;

    const action = user.isActive ? 'bloquer' : 'débloquer';
    const fullName = `${user.firstname} ${user.lastname}`;

    const confirmed = await this.confirmationService.confirm({
      title: user.isActive ? 'Bloquer l\'utilisateur' : 'Débloquer l\'utilisateur',
      message: `Voulez-vous ${action} ${fullName} ? Cette action peut être annulée en débloqu${action === 'bloquer' ? 'a' : 'é'}nt le compte.`,
      confirmText: action === 'bloquer' ? 'Bloquer' : 'Débloquer',
      cancelText: 'Annuler',
      variant: action === 'bloquer' ? 'danger' : 'default'
    });

    if (!confirmed) return;

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

  /**
   * Supprime un utilisateur avec modal de confirmation
   */
  async deleteUser(user: GsUser): Promise<void> {
    if (!this.canManageUser(user)) return;

    const fullName = `${user.firstname} ${user.lastname}`;

    const confirmed = await this.confirmationService.confirm({
      title: 'Supprimer l\'utilisateur',
      message: `Supprimer "${fullName}" ? Cette action est irréversible.`,
      confirmText: 'Supprimer',
      cancelText: 'Annuler',
      variant: 'danger'
    });

    if (!confirmed) return;

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
