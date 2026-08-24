import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Materiel } from '../models/materiel.model';

@Injectable({
  providedIn: 'root'
})
export class MaterielService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/materiels';

  // Récupérer tous les matériels (avec pagination)
  getAll(): Observable<Materiel[]> {
    return this.http.get<any>(this.apiUrl).pipe(
      map((res) => {
        // Si l'API renvoie { content: [...] }
        if (res && res.content) {
          return res.content;
        }
        // Sinon, on renvoie directement le tableau
        return res;
      })
    );
  }

  // Récupérer un matériel par ID
  getById(id: number): Observable<Materiel> {
    return this.http.get<Materiel>(`${this.apiUrl}/${id}`);
  }
}