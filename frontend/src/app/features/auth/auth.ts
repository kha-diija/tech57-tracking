import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../shared/services/auth.service';
import { ApiErrorResponse } from '../../shared/models/auth.model';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.scss',
})
export class Auth {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(false);
  readonly showPassword = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(6)]],
  });

  togglePasswordVisibility(): void {
    this.showPassword.update((value) => !value);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, motDePasse } = this.loginForm.getRawValue();

    this.authService.login({ email: email!, motDePasse: motDePasse! }).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.authService.redirectAfterLogin(response.redirectUrl);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        const apiError = err.error as ApiErrorResponse | undefined;
        this.errorMessage.set(
          apiError?.message ?? 'Impossible de se connecter. Veuillez réessayer.'
        );
      },
    });
  }

  onGoogleLogin(): void {
    // À brancher sur l'endpoint OAuth2 Google du backend
    console.info('Connexion Google : à implémenter côté backend (OAuth2).');
  }

  get email() {
    return this.loginForm.controls.email;
  }

  get motDePasse() {
    return this.loginForm.controls.motDePasse;
  }
}