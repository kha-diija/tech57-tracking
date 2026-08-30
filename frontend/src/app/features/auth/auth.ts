import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
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
  private readonly router = inject(Router);

  readonly isLoading = signal(false);
  readonly showPassword = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor() {
    // Si l'utilisateur arrive ici suite à une déconnexion forcée
    // (refresh token expiré/révoqué), on affiche le message explicatif
    // transmis par AuthService.clearSessionAndRedirect() via le router state.
    const navState = this.router.getCurrentNavigation()?.extras?.state;
    const sessionMessage = (navState ?? window.history.state)?.['sessionMessage'];
    if (sessionMessage) {
      this.errorMessage.set(sessionMessage);
    }
  }

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
    // (ex: window.location.href = `${environment.apiUrl}/oauth2/authorization/google`;)
    console.info('Connexion Google : à implémenter côté backend (OAuth2).');
  }

  get email() {
    return this.loginForm.controls.email;
  }

  get motDePasse() {
    return this.loginForm.controls.motDePasse;
  }
}