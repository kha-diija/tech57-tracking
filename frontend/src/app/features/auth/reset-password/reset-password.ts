import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule, FormBuilder, Validators,
  AbstractControl, ValidationErrors
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../shared/services/auth.service';
import { ApiErrorResponse } from '../../../shared/models/auth.model';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const pass = group.get('nouveauMotDePasse')?.value;
  const confirm = group.get('confirmation')?.value;
  return pass === confirm ? null : { mismatch: true };
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss',
})
export class ResetPassword implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private token = '';

  readonly isLoading = signal(false);
  readonly tokenMissing = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly resetForm = this.fb.group(
    {
      nouveauMotDePasse: ['', [Validators.required, Validators.minLength(6)]],
      confirmation: ['', [Validators.required, Validators.minLength(6)]],
    },
    { validators: passwordsMatchValidator }
  );

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.tokenMissing.set(true);
    }
  }

  onSubmit(): void {
    if (this.resetForm.invalid || !this.token) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { nouveauMotDePasse } = this.resetForm.getRawValue();

    this.authService.resetPassword(this.token, nouveauMotDePasse!).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.successMessage.set(response.message);
        setTimeout(() => this.router.navigateByUrl('/login'), 2500);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        const apiError = err.error as ApiErrorResponse | undefined;
        this.errorMessage.set(
          apiError?.message ?? 'Lien invalide ou expiré. Refaites une demande.'
        );
      },
    });
  }

  get nouveauMotDePasse() {
    return this.resetForm.controls.nouveauMotDePasse;
  }

  get confirmation() {
    return this.resetForm.controls.confirmation;
  }
}