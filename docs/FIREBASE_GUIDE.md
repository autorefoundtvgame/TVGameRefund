# Guide d'utilisation de Firebase dans TVGameRefund

Ce document explique comment Firebase est utilisé dans l'application TVGameRefund et comment implémenter de nouvelles fonctionnalités utilisant Firebase.

## Qu'est-ce que Firebase ?

Firebase est une plateforme de développement d'applications mobiles et web proposée par Google. Elle offre de nombreux services qui nous permettent de construire, améliorer et développer notre application sans avoir à gérer l'infrastructure serveur nous-mêmes.

## Services Firebase utilisés dans notre application

### 1. Firebase Firestore

Firestore est une base de données NoSQL dans le cloud qui nous permet de stocker et synchroniser les données entre les utilisateurs en temps réel.

**Utilisation dans notre application :**
- Stockage des votes des utilisateurs pour les jeux TV
- Stockage des statistiques de remboursement
- Partage d'expériences entre utilisateurs

**Structure des données :**
```
/votes/{voteId}
  - userId: String
  - gameId: String
  - rating: Int
  - comment: String?
  - date: Timestamp

/gameStats/{gameId}
  - totalVotes: Int
  - averageRating: Double
  - successfulRefunds: Int
  - failedRefunds: Int
  - averageRefundTime: Int (en jours)
```

### 2. Firebase Authentication (optionnel)

Firebase Authentication permet de gérer l'authentification des utilisateurs de manière sécurisée.

**Utilisation potentielle :**
- Authentification anonyme pour suivre les votes et participations
- Authentification par email/mot de passe si nous décidons d'ajouter des comptes utilisateurs

### 3. Firebase Analytics

Firebase Analytics nous permet de suivre l'utilisation de l'application et de comprendre comment les utilisateurs interagissent avec elle.

**Métriques suivies :**
- Nombre de jeux consultés
- Nombre de participations enregistrées
- Taux de succès des remboursements
- Engagement des utilisateurs

## Comment utiliser Firebase dans le code

### Configuration

La configuration Firebase est déjà présente dans le projet via le fichier `google-services.json`. Les dépendances nécessaires sont également configurées dans le fichier `build.gradle.kts`.

### Exemple d'utilisation de Firestore

```kotlin
// Soumettre un vote pour un jeu
fun submitVote(gameId: String, rating: Int, comment: String?) {
    val vote = UserVote(
        id = UUID.randomUUID().toString(),
        userId = "current_user", // À remplacer par l'ID de l'utilisateur connecté
        gameId = gameId,
        rating = rating,
        comment = comment,
        date = Date()
    )
    
    // Ajouter le vote à Firestore
    FirebaseFirestore.getInstance()
        .collection("votes")
        .document(vote.id)
        .set(vote)
        .addOnSuccessListener {
            // Vote enregistré avec succès
        }
        .addOnFailureListener { e ->
            // Erreur lors de l'enregistrement du vote
        }
}

// Récupérer les statistiques d'un jeu
fun getGameStats(gameId: String, onSuccess: (GameStats) -> Unit, onFailure: (Exception) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("gameStats")
        .document(gameId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val gameStats = document.toObject(GameStats::class.java)
                gameStats?.let { onSuccess(it) }
            } else {
                onSuccess(GameStats(gameId = gameId))
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}
```

### Utilisation avec Kotlin Coroutines

Pour une meilleure intégration avec Kotlin Coroutines, nous pouvons utiliser des extensions comme celles-ci :

```kotlin
suspend fun submitVoteCoroutine(vote: UserVote): Result<UserVote> = suspendCoroutine { continuation ->
    FirebaseFirestore.getInstance()
        .collection("votes")
        .document(vote.id)
        .set(vote)
        .addOnSuccessListener {
            continuation.resume(Result.success(vote))
        }
        .addOnFailureListener { e ->
            continuation.resume(Result.failure(e))
        }
}
```

## Bonnes pratiques

1. **Sécurité des données :** Assurez-vous de mettre en place des règles de sécurité Firestore appropriées pour protéger les données des utilisateurs.

2. **Gestion hors ligne :** Configurez Firestore pour fonctionner hors ligne afin que les utilisateurs puissent continuer à utiliser l'application sans connexion Internet.

3. **Pagination :** Pour les grandes collections, utilisez la pagination pour éviter de charger trop de données à la fois.

4. **Transactions :** Utilisez des transactions Firestore pour les opérations qui nécessitent une cohérence des données.

## Ressources utiles

- [Documentation Firebase](https://firebase.google.com/docs)
- [Guide Firestore pour Android](https://firebase.google.com/docs/firestore/quickstart)
- [Firebase avec Kotlin Coroutines](https://firebase.google.com/docs/android/kotlin-coroutines)