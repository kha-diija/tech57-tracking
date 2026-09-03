import { Component, inject, signal, computed, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Message {
  sender: 'user' | 'ia';
  text: string;
}

interface Suggestion {
  label: string;      // texte court affiché sur le chip
  question: string;    // question complète envoyée à l'assistant
  type: 'sql' | 'rag'; // 'sql' -> réponse instantanée DB, 'rag' -> recherche documentaire
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

  // Suggestions affichées uniquement au tout début de la conversation
  showSuggestions = computed(() => this.messages().length <= 1 && !this.loading());

  readonly suggestions: Suggestion[] = [
    // Questions "chiffrées" -> répondues directement par SQL, sans passer par le LLM
    { label: 'Total matériel', question: 'Combien de matériel avons-nous au total ?', type: 'sql' },
    { label: 'Établissements', question: 'Liste des établissements enregistrés', type: 'sql' },
    { label: 'Missions terminées', question: 'Combien de missions sont Terminée ?', type: 'sql' },
    // Questions ouvertes -> recherche dans les documents internes (RAG) puis génération LLM
    { label: 'Cahier des charges', question: 'Que dit le cahier des charges sur les objectifs du projet ?', type: 'rag' },
    { label: 'Règles de gestion', question: 'Quelles sont les règles de gestion à connaître ?', type: 'rag' },
    { label: 'Suivi interventions', question: 'Comment fonctionne le suivi des interventions ?', type: 'rag' },
  ];

  get sqlSuggestions() {
    return this.suggestions.filter(s => s.type === 'sql');
  }

  get ragSuggestions() {
    return this.suggestions.filter(s => s.type === 'rag');
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  useSuggestion(suggestion: Suggestion) {
    if (this.loading()) return;
    this.userMessage.set(suggestion.question);
    this.sendMessage();
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