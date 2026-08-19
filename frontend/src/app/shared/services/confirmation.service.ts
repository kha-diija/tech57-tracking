import { Injectable, signal } from '@angular/core';

export type ConfirmationVariant = 'default' | 'danger' | 'warning';

export interface ConfirmDialogConfig {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: ConfirmationVariant; // 'default' (bleu) | 'danger' (rouge) | 'warning' (orange)
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {
  isOpen = signal<boolean>(false);
  config = signal<ConfirmDialogConfig>({
    title: '',
    message: '',
    confirmText: 'Confirmer',
    cancelText: 'Annuler',
    variant: 'default'
  });

  private resolveCallback: ((value: boolean) => void) | null = null;

  confirm(dialogConfig: ConfirmDialogConfig): Promise<boolean> {
    return new Promise((resolve) => {
      this.config.set({
        confirmText: 'Confirmer',
        cancelText: 'Annuler',
        variant: 'default',
        ...dialogConfig
      });
      this.isOpen.set(true);
      this.resolveCallback = resolve;
    });
  }

  onConfirm(): void {
    this.closeDialog(true);
  }

  onCancel(): void {
    this.closeDialog(false);
  }

  private closeDialog(result: boolean): void {
    this.isOpen.set(false);
    if (this.resolveCallback) {
      this.resolveCallback(result);
      this.resolveCallback = null;
    }
  }
}
