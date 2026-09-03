import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from '../../services/confirmation.service';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="confirmation-overlay" *ngIf="confirmationService.isOpen()" (click)="confirmationService.onCancel()">
      <div class="confirmation-modal" [class]="'variant-' + (confirmationService.config().variant || 'default')" (click)="$event.stopPropagation()">
        <div class="confirmation-header">
          <h2>{{ confirmationService.config().title }}</h2>
          <button class="close-btn" (click)="confirmationService.onCancel()">✕</button>
        </div>

        <div class="confirmation-body">
          {{ confirmationService.config().message }}
        </div>

        <div class="confirmation-actions">
          <button class="btn-cancel" (click)="confirmationService.onCancel()">
            {{ confirmationService.config().cancelText }}
          </button>
          <button 
            class="btn-confirm" 
            [class.btn-danger]="confirmationService.config().variant === 'danger'"
            [class.btn-warning]="confirmationService.config().variant === 'warning'"
            (click)="confirmationService.onConfirm()">
            {{ confirmationService.config().confirmText }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .confirmation-overlay {
      position: fixed;
      inset: 0;
      background-color: rgba(0, 0, 0, 0.65);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1001;
      padding: 1rem;
      box-sizing: border-box;
    }

    .confirmation-modal {
      background-color: var(--bg-surface);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      width: 100%;
      max-width: 420px;
      padding: 1.5rem;
      color: var(--text-main);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      gap: 1rem;
      animation: slideIn 0.2s ease-out;
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: scale(0.95);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }

    .confirmation-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 0.75rem;

      h2 {
        margin: 0;
        font-size: 1.125rem;
        font-weight: 700;
        color: var(--text-main);
      }

      .close-btn {
        background: none;
        border: none;
        color: var(--text-muted);
        font-size: 1.25rem;
        cursor: pointer;
        padding: 0;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: color 0.2s ease;

        &:hover {
          color: var(--text-main);
        }
      }
    }

    .confirmation-body {
      font-size: 0.9375rem;
      line-height: 1.6;
      color: var(--text-main);
      padding: 0.5rem 0;
    }

    .confirmation-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      border-top: 1px solid var(--border-color);
      padding-top: 1rem;
      margin-top: 0.5rem;

      .btn-cancel {
        background: none;
        border: none;
        color: var(--text-muted);
        padding: 0.6rem 1.2rem;
        cursor: pointer;
        border-radius: 6px;
        font-weight: 500;
        transition: all 0.2s ease;
        font-size: 0.875rem;

        &:hover {
          background-color: var(--bg-app);
          color: var(--text-main);
        }

        &:active {
          background-color: var(--bg-surface-hover);
        }
      }

      .btn-confirm {
        background-color: var(--primary);
        color: #ffffff;
        padding: 0.6rem 1.2rem;
        border-radius: 6px;
        font-weight: 600;
        border: none;
        cursor: pointer;
        font-size: 0.875rem;
        transition: opacity 0.2s ease;

        &:hover {
          opacity: 0.9;
        }

        &:active {
          opacity: 0.8;
        }

        &.btn-danger {
          background-color: #EF4444;

          &:hover {
            opacity: 0.9;
          }
        }

        &.btn-warning {
          background-color: #d74800;
          color: #1f2937;

          &:hover {
            opacity: 0.9;
          }
        }
      }
    }

    @media (max-width: 640px) {
      .confirmation-modal {
        max-width: 100%;
        padding: 1.25rem;
      }

      .confirmation-header h2 {
        font-size: 1rem;
      }

      .confirmation-body {
        font-size: 0.875rem;
      }

      .confirmation-actions {
        gap: 0.5rem;

        button {
          padding: 0.5rem 1rem;
          font-size: 0.8125rem;
        }
      }
    }
  `]
})
export class ConfirmationDialogComponent {
  confirmationService = inject(ConfirmationService);
}