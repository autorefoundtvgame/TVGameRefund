# Guide de contribution

Merci de votre intérêt pour contribuer à TVGameRefund ! Voici quelques lignes directrices pour vous aider à contribuer efficacement.

## Comment contribuer

### Signaler des bugs

Si vous trouvez un bug, veuillez créer une issue en incluant :
- Une description claire du problème
- Les étapes pour reproduire le bug
- Le comportement attendu et le comportement observé
- Des captures d'écran si possible
- Votre environnement (version Android, appareil, etc.)

### Proposer des améliorations

Pour proposer une amélioration :
- Créez une issue décrivant votre idée
- Expliquez pourquoi cette fonctionnalité serait utile
- Suggérez une approche pour l'implémentation si possible

### Soumettre du code

1. Forkez le dépôt
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/amazing-feature`)
3. Committez vos changements (`git commit -m 'Add some amazing feature'`)
4. Poussez vers la branche (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

## Standards de code

### Style de code

- Suivez les conventions de style Kotlin standard
- Utilisez des noms de variables et de fonctions descriptifs
- Commentez votre code lorsque nécessaire
- Respectez l'architecture MVVM du projet

### Tests

- Ajoutez des tests unitaires pour les nouvelles fonctionnalités
- Assurez-vous que tous les tests passent avant de soumettre une PR

## Sécurité

- Ne commettez jamais de clés API, mots de passe ou autres secrets
- Utilisez les mécanismes de stockage sécurisé pour les données sensibles
- Signalez immédiatement toute vulnérabilité de sécurité potentielle

## Processus de revue

- Toutes les Pull Requests seront revues par au moins un mainteneur
- Les commentaires de revue doivent être adressés avant la fusion
- Les PR doivent passer tous les tests automatisés

## Licence

En contribuant à ce projet, vous acceptez que vos contributions soient sous la même licence que le projet (MIT).