import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../shared/services/auth.service';
import { ApiErrorResponse } from '../../../shared/models/auth.model';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly forgotForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  onSubmit(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const { email } = this.forgotForm.getRawValue();

    this.authService.forgotPassword(email!).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.successMessage.set(response.message);
        this.forgotForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        const apiError = err.error as ApiErrorResponse | undefined;
        this.errorMessage.set(apiError?.message ?? "Impossible d'envoyer l'email. Réessayez.");
      },
    });
  }

  get email() {
    return this.forgotForm.controls.email;
  }
}