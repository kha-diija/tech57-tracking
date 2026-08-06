import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { SettingsService } from '../../shared/services/settings.service';

interface PasswordStrength {
  score: number;   // 0 à 4
  label: string;
  color: string;
}

// Doit rester synchronisé avec le pattern du backend (@Pattern dans ChangePasswordRequest)
function strongPasswordValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value as string;
  if (!value) return null;
  const hasLower = /[a-z]/.test(value);
  const hasUpper = /[A-Z]/.test(value);
  const hasDigit = /\d/.test(value);
  return hasLower && hasUpper && hasDigit ? null : { weakPassword: true };
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './settings.html',
  styleUrls: ['./settings.scss'],
})
export class Settings {
  passwordForm: FormGroup;
  isLoading = false;
  successMessage = '';
  errorMessage = '';

  strength: PasswordStrength = { score: 0, label: '', color: '' };

  showAncien = false;
  showNouveau = false;
  showConfirmer = false;

  constructor(
    private fb: FormBuilder,
    private settingsService: SettingsService
  ) {
    this.passwordForm = this.fb.group(
      {
        ancienMotDePasse: ['', [Validators.required]],
        nouveauMotDePasse: ['', [Validators.required, Validators.minLength(8), strongPasswordValidator]],
        confirmerMotDePasse: ['', [Validators.required]],
      },
      { validators: this.passwordMatchValidator }
    );

    this.passwordForm.get('nouveauMotDePasse')?.valueChanges.subscribe((value: string) => {
      this.strength = this.calculatePasswordStrength(value);
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const nouveau = form.get('nouveauMotDePasse')?.value;
    const confirmer = form.get('confirmerMotDePasse')?.value;
    return nouveau === confirmer ? null : { mismatch: true };
  }

  // Score simple 0-4 basé sur longueur / diversité de caractères
  calculatePasswordStrength(password: string): PasswordStrength {
    if (!password) return { score: 0, label: '', color: '' };

    let score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    const levels: Omit<PasswordStrength, 'score'>[] = [
      { label: 'Très faible', color: '#ef4444' },
      { label: 'Faible', color: '#f97316' },
      { label: 'Moyen', color: '#eab308' },
      { label: 'Fort', color: '#22c55e' },
      { label: 'Très fort', color: '#16a34a' },
    ];

    const idx = Math.min(score, levels.length - 1);
    return { score: idx, ...levels[idx] };
  }

  togglePassword(field: 'ancien' | 'nouveau' | 'confirmer'): void {
    if (field === 'ancien') this.showAncien = !this.showAncien;
    if (field === 'nouveau') this.showNouveau = !this.showNouveau;
    if (field === 'confirmer') this.showConfirmer = !this.showConfirmer;
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const { ancienMotDePasse, nouveauMotDePasse } = this.passwordForm.value;

    this.settingsService.changePassword({ ancienMotDePasse, nouveauMotDePasse }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = res.message || 'Mot de passe modifié avec succès.';
        this.passwordForm.reset();
        this.strength = { score: 0, label: '', color: '' };
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du changement de mot de passe.';
      },
    });
  }
}