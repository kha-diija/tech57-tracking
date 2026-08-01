import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EquipeTechniqueService {
  private readonly http = inject(HttpClient);
  // Adapte l'URL de l'API selon le chemin configuré dans ton contrôleur Spring Boot
  private readonly apiUrl = 'http://localhost:8080/api/equipes'; 

  // Récupérer la liste de toutes les équipes techniques
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}