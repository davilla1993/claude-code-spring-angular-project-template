# Claude Code - Spring Boot + Angular Project Template

Template d'initialisation pour projets professionnels en monorepo.

## Structure cible
```text
project/
├── cahier-des-charges.md
├── CLAUDE.md
├── agents.md
├── README.md
├── docs/
├── backend/
│   └── CLAUDE.md
└── frontend/
    └── CLAUDE.md
```

## Démarrage
1. Créer le dossier du projet.
2. Ajouter le cahier des charges.
3. Copier ce template à la racine.
4. Générer le backend Spring Boot.
5. Générer le frontend Angular.
6. Copier les CLAUDE.md spécifiques dans backend/ et frontend/.
7. Lancer Claude Code à la racine.
8. Demander le bootstrap du projet.

Prompt recommandé :
> Lis intégralement le cahier des charges et les règles du dépôt. Ne code rien. Exécute le bootstrap défini dans CLAUDE.md : analyse, exigences, architecture, documentation et plan d'implémentation. Arrête-toi avant toute implémentation et présente les décisions et questions nécessitant validation.

## Points d'attention
- **Flexibilité du processus** : Le bootstrap et les phases de documentation sont rigoureux pour garantir la qualité. Cependant, pour des modifications mineures ou des corrections triviales, vous pouvez demander à Claude de simplifier le processus pour éviter une bureaucratie excessive.
- **Synchronisation API** : Le point le plus critique est la cohérence entre le contrat API (`docs/API_CONTRACT.md`) et le code implémenté. Veillez à toujours demander à Claude de vérifier que tout changement de code est reflété dans le contrat API avant de valider la tâche.
