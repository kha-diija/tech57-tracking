import { Component, inject, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Message {
  sender: 'user' | 'ia';
  text: string;
}

@Component({
  selector: 'app-chat-ia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-ia.html',
  styleUrl: './chat-ia.scss',
})
export class ChatIa implements AfterViewChecked {
  private http = inject(HttpClient);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  // Message d'accueil simple et chaleureux
  messages = signal<Message[]>([
    { sender: 'ia', text: 'Bonjour ! Je suis l\'assistant IA de Tech57. Comment puis-je vous aider aujourd\'hui ?' }
  ]);
  userMessage = signal<string>('');
  loading = signal<boolean>(false);

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  sendMessage() {
    const text = this.userMessage().trim();
    if (!text || this.loading()) return;

    this.messages.update((msgs) => [...msgs, { sender: 'user', text }]);
    this.userMessage.set('');
    this.loading.set(true);

    this.http.post<{ response: string }>('http://localhost:8080/api/ia/chat', { message: text })
      .subscribe({
        next: (res) => {
          this.messages.update((msgs) => [...msgs, { sender: 'ia', text: res.response }]);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Erreur Chat IA:', err);
          this.messages.update((msgs) => [
            ...msgs,
            // Message d'erreur simple et compréhensible
            { sender: 'ia', text: 'Désolé, une erreur est survenue lors de l\'envoi du message. Veuillez réessayer.' }
          ]);
          this.loading.set(false);
        }
      });
  }
}