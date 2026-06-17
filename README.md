# # PlanningPFE

Application de bureau (Java / Swing) pour la gestion et la génération automatique des plannings de soutenances de Projets de Fin d'Études (PFE).

## Fonctionnalités

- Chargement des données (étudiants, enseignants, salles, configuration) depuis un fichier Excel
- Affectation automatique des encadrants aux étudiants
- Génération automatique du planning des soutenances en respectant les contraintes (disponibilité des enseignants, écart minimum entre soutenances, capacité des salles, pause déjeuner, etc.)
- Distribution équilibrée de la charge entre les enseignants
- Visualisation du planning généré dans l'interface
- Vérification des contraintes et détection d'anomalies
- Tableau de bord (statistiques sur les soutenances, la charge des enseignants, etc.)
- Historique des plannings générés (sauvegarde et restauration)
- Export du planning et des fiches de notation au format Word (.docx)

## Prérequis

- Java JDK 17 ou version supérieure
- Eclipse IDE (recommandé)
- Les bibliothèques Apache POI (lecture/écriture des fichiers Excel et Word)

## Installation et lancement

1. Cloner le dépôt :
   ```
   git clone https://github.com/lebyedhajar-lab/planningPFE.git
   ```
2. Ouvrir le projet dans Eclipse via **File → Import → Existing Projects into Workspace**
3. Vérifier que les bibliothèques (JARs) sont bien ajoutées au Build Path du projet (onglet **Libraries**)
4. Lancer l'application en exécutant la classe `ui.MainFrame`

## Utilisation

1. Charger le fichier Excel de configuration via le menu **Charger Excel**
2. Générer le planning via le menu **Générer Planning**
3. Consulter le résultat dans **Voir Planning**
4. Exporter le planning et les fiches de notation via le menu **Exporter**

## Structure du projet

```
src/
├── algorithm/    → Algorithmes de génération et distribution du planning
├── Config/       → Configuration du planning
├── deliberation/ → Gestion des délibérations
├── export/       → Export des documents (planning, fiches de notation)
├── historique/   → Historique des plannings générés
├── loader/       → Chargement des données depuis Excel
├── model/        → Classes métier (Étudiant, Enseignant, Soutenance, etc.)
├── repository/   → Gestion des données en mémoire
├── statistiques/ → Tableau de bord et statistiques
└── ui/           → Interface graphique (Swing)
```

## Auteurs

Projet réalisé dans le cadre du Projet de Fin d'Études (PFE) à l'ENSAH (École Nationale des Sciences Appliquées d'Al-Hoceima).
