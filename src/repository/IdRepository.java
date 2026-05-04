package repository;

import java.util.List;

public interface IdRepository<T> {
	
    void sauvegarder(T objet);
    List<T> chargerTous();
    T trouverParId(int id);
    boolean supprimer(int id);
  
}
