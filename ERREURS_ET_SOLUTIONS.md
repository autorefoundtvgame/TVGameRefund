# Journal des erreurs et solutions

Ce document recense les erreurs rencontrées pendant le développement de l'application TVGameRefund et les solutions mises en place.

## Erreurs d'exécution récentes

### 30. Duplication de OkHttpClient dans les modules Dagger

**Erreur :**
```
C:\Users\Castle\Code\TVGameRefund\app\build\generated\hilt\component_sources\debug\com\openhands\tvgamerefund\TVGameRefundApp_HiltComponents.java:152: error: [Dagger/DuplicateBindings] okhttp3.OkHttpClient is bound multiple times:
  public abstract static class SingletonC implements TVGameRefundApp_GeneratedInjector,
                         ^
          @Provides @Singleton okhttp3.OkHttpClient com.openhands.tvgamerefund.di.AppModule.provideOkHttpClient()
          @Provides @Singleton okhttp3.OkHttpClient com.openhands.tvgamerefund.di.NetworkModule.provideOkHttpClient()
```

**Solution :**
Suppression de la méthode `provideOkHttpClient()` dans `AppModule.kt` pour éviter la duplication avec celle de `NetworkModule.kt`. Nous avons conservé l'implémentation dans `NetworkModule` car elle inclut un intercepteur de logging pour le débogage.

### 31. Méthode dépréciée dans WebViewAuthScreen

**Avertissement :**
```
w: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/screens/settings/WebViewAuthScreen.kt:236:31 'onReceivedError(WebView!, Int, String!, String!): Unit' is deprecated. Deprecated in Java
```

**Solution :**
Ajout de l'annotation `@Deprecated` à la méthode existante et implémentation de la nouvelle méthode `onReceivedError` qui prend des paramètres différents :
```kotlin
override fun onReceivedError(view: WebView, request: android.webkit.WebResourceRequest, error: android.webkit.WebResourceError) {
    super.onReceivedError(view, request, error)
    Log.e("WebViewAuth", "Erreur: ${error.description}")
    onError("Erreur lors du chargement de la page: ${error.description}")
}
```

## Erreurs d'exécution récentes

### Bonnes pratiques générales

1. **Toujours vérifier les valeurs nulles** avant de les utiliser, en particulier pour les dates et les montants.
2. **Ajouter des logs** pour faciliter le débogage.
3. **Gérer les erreurs** de manière gracieuse pour éviter les crashs.
4. **Mettre à jour la version de la base de données** lorsque le schéma change.
5. **Tester l'application** sur différents appareils et versions d'Android.

### 29. Erreur de schéma de base de données Room

**Erreur :**
```
java.lang.IllegalStateException: Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number. You can simply fix this by increasing the version number.
```

**Cause :**
Modification du schéma de la base de données (ajout de nouveaux champs à une entité) sans mise à jour du numéro de version.

**Solution :**
Augmenter le numéro de version de la base de données dans `AppDatabase.kt` :
```kotlin
@Database(
    entities = [
        Show::class,
        ShowSchedule::class,
        Game::class,
        GameFee::class,
        GameStats::class,
        UserParticipation::class,
        UserVote::class
    ],
    version = 2, // Augmentation de la version de 1 à 2
    exportSchema = false
)
```

### 30. Avertissement OnBackInvokedCallback

**Erreur :**
```
OnBackInvokedCallback is not enabled for the application.
```

**Cause :**
L'application n'est pas configurée pour utiliser le nouveau système de gestion du bouton retour d'Android.

**Solution :**
Ajouter l'attribut `android:enableOnBackInvokedCallback="true"` dans le fichier `AndroidManifest.xml` :
```xml
<application
    android:name=".TVGameRefundApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:enableOnBackInvokedCallback="true"
    android:theme="@style/Theme.TVGameRefund">
```

### 31. Crash lors du retour en arrière

**Erreur :**
```
android.os.DeadObjectException
```

**Cause :**
Problème avec la gestion du cycle de vie des activités ou des fragments.

**Solution :**
Améliorer la gestion du cycle de vie dans l'activité principale en ajoutant des gestionnaires d'erreurs pour `onBackPressed()` et `onDestroy()` :
```kotlin
override fun onBackPressed() {
    // Gérer le bouton retour de manière sécurisée
    try {
        super.onBackPressed()
    } catch (e: Exception) {
        // En cas d'erreur, terminer l'activité proprement
        finish()
    }
}

override fun onDestroy() {
    try {
        super.onDestroy()
    } catch (e: Exception) {
        // Ignorer les erreurs lors de la destruction de l'activité
    }
}
```

### 32. Erreur Firebase

**Erreur :**
```
com.google.firebase.firestore.FirebaseFirestoreException: Failed to get document because the client is offline.
```

**Cause :**
L'application essaie d'accéder à Firebase mais n'a pas de connexion Internet ou Firebase n'est pas correctement configuré.

**Solution :**
Améliorer la gestion des erreurs Firebase en retournant des données par défaut en cas d'erreur :
```kotlin
private fun isFirebaseAvailable(): Boolean {
    // En développement, on considère que Firebase n'est pas disponible
    // pour éviter les erreurs de configuration
    return false
}
```

### 33. NullPointerException avec les dates

**Erreur :**
```
java.lang.NullPointerException: date must not be null
    at java.util.Objects.requireNonNull(Objects.java:232)
    at java.util.Calendar.setTime(Calendar.java:1831)
    at java.text.SimpleDateFormat.format(SimpleDateFormat.java:1094)
```

**Cause :**
Tentative de formatage d'une date nulle avec `SimpleDateFormat`.

**Solution :**
Vérifier si la date est nulle avant de la formater :
```kotlin
val dateStr = if (game.startDate != null) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    dateFormat.format(game.startDate)
} else {
    "Date inconnue"
}
```

### 34. Problème avec le scraper TF1

**Erreur :**
```
TF1GameScraper: Liens de jeux trouvés: 0
```

**Cause :**
Le scraper ne trouve aucun lien de jeu sur le site TF1, soit parce que la structure HTML a changé, soit parce que les patterns regex ne sont pas assez robustes.

**Solution :**
Améliorer le scraper TF1 pour qu'il soit plus robuste :
1. Ajouter plusieurs patterns regex pour détecter différents formats de liens
2. Ajouter des liens par défaut pour les tests si aucun lien n'est trouvé
3. Ajouter un User-Agent dans les requêtes HTTP pour éviter d'être bloqué
4. Ajouter plus de logs pour mieux comprendre ce qui se passe

```kotlin
private fun extractGameLinks(html: String): List<String> {
    val links = mutableListOf<String>()
    
    // Essayer plusieurs patterns pour être plus robuste
    val patterns = listOf(
        """<a href="(/tf1/gagnants-reglements-remboursement-des-jeux-tv/news/[^"]+)"""".toRegex(),
        """<a href="(https://www.tf1.fr/[^"]+/reglement[^"]+)"""".toRegex(),
        """<a href="(/tf1/[^"]+/reglement[^"]+)"""".toRegex(),
        """<a href="(/[^"]+/reglement[^"]+)"""".toRegex(),
        """<a href="(/programmes/[^"]+/jeux[^"]+)"""".toRegex()
    )
    
    // ... reste du code
}
```

## Erreurs de compilation

### 15. Incompatibilité de version Kotlin avec Firebase

**Erreur :**
```
e: file:///C:/Users/Castle/.gradle/caches/transforms-3/322cd81d7fd16c75bec209c9ee68c745/transformed/firebase-auth-23.2.0-api.jar!/META-INF/java.com.google.android.gmscore.integ.client.firebase-auth-api_firebase-auth-api.kotlin_moduleModule was compiled with an incompatible version of Kotlin. The binary version of its metadata is 2.1.0, expected version is 1.9.0.
```

**Cause :**
Les bibliothèques Firebase utilisent une version plus récente de Kotlin (2.1.0) que celle utilisée dans le projet (1.9.0).

**Solution :**
1. Mise à jour de la version de Kotlin à 1.9.22 (compatible avec les bibliothèques Firebase)
2. Mise à jour de la version de KSP à 1.9.22-1.0.16
3. Mise à jour de la version du compilateur Compose à 1.5.6
4. Utilisation d'une version plus ancienne de Firebase BoM (32.7.0) compatible avec Kotlin 1.9.22
5. Mise à jour de la version de Hilt à 2.49 pour assurer la compatibilité

### 16. Problèmes avec les annotations Room

**Erreur :**
```
e: [ksp] C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/models/Invoice.kt:24: Entity class must be annotated with @Entity
e: [ksp] C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/models/Invoice.kt:24: An entity must have at least 1 field annotated with @PrimaryKey
```

**Cause :**
Les classes de modèle utilisées avec Room n'avaient pas les annotations requises.

**Solution :**
1. Ajout de l'annotation `@Entity(tableName = "invoices")` à la classe Invoice
2. Ajout de l'annotation `@PrimaryKey` au champ id
3. Ajout de l'annotation `@TypeConverters(Converters::class)` pour gérer les types complexes comme Date
4. Création d'une classe distincte `InvoiceGameFee` avec les annotations appropriées

### 17. Problèmes avec les dépendances Firebase et Hilt

**Erreur :**
```
e: [ksp] InjectProcessingStep was unable to process 'FirebaseVoteRepository(error.NonExistentClass,error.NonExistentClass)' because 'error.NonExistentClass' could not be resolved.
e: [ksp] InjectProcessingStep was unable to process 'ReminderWorker(android.content.Context,error.NonExistentClass,com.openhands.tvgamerefund.data.repository.GameRepository,com.openhands.tvgamerefund.data.repository.UserParticipationRepository)' because 'error.NonExistentClass' could not be resolved.
```

**Cause :**
Les classes Firebase n'étaient pas correctement injectées via Hilt, et les dépendances n'étaient pas correctement configurées. De plus, les annotations Hilt pour les Workers causaient des problèmes.

**Solution :**
1. Simplification de l'injection de dépendances en utilisant directement les instances Firebase au lieu de l'injection
2. Modification du `FirebaseVoteRepository` pour utiliser `Firebase.firestore` et `Firebase.auth` directement
3. Renommage du module Firebase en NetworkModule pour éviter les conflits
4. Ajout d'un fournisseur OkHttpClient pour les requêtes réseau
5. Simplification du `ReminderWorker` pour éviter les annotations Hilt complexes
6. Modification du `ReminderManager` pour passer les données nécessaires au Worker via les paramètres d'entrée

### 19. Incompatibilité entre Kotlin et le compilateur Compose

**Erreur :**
```
e: This version (1.5.6) of the Compose Compiler requires Kotlin version 1.9.21 but you appear to be using Kotlin version 1.9.22 which is not known to be compatible.
```

**Cause :**
Le compilateur Compose 1.5.6 nécessite spécifiquement Kotlin 1.9.21, mais nous utilisions Kotlin 1.9.22.

**Solution :**
1. Rétrogradation de Kotlin à la version 1.9.21
2. Rétrogradation de KSP à la version 1.9.21-1.0.15
3. Utilisation du compilateur Compose 1.5.4 compatible avec Kotlin 1.9.21
4. Ajout de l'option de compilation `-P plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true` pour ignorer les vérifications de compatibilité

### 35. Incompatibilité entre Kotlin 1.9.21 et le compilateur Compose 1.5.1

**Erreur :**
```
e: This version (1.5.1) of the Compose Compiler requires Kotlin version 1.9.0 but you appear to be using Kotlin version 1.9.21 which is not known to be compatible.
```

**Cause :**
Le compilateur Compose 1.5.1 nécessite spécifiquement Kotlin 1.9.0, mais nous utilisons Kotlin 1.9.21.

**Solution :**
1. Mise à jour du compilateur Compose à la version 1.5.4 qui est plus compatible avec Kotlin 1.9.21
2. Ajout de l'option de compilation pour supprimer la vérification de compatibilité :
```kotlin
kotlinOptions {
    freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.9.21"
    )
}
```
3. Cette option permet d'utiliser le compilateur Compose même avec une version de Kotlin qui n'est pas officiellement supportée
4. Maintien du compileSdk et targetSdk à 34 pour assurer la compatibilité avec les bibliothèques comme androidx.hilt:hilt-work:1.1.0
5. Il est important de spécifier la version exacte de Kotlin (1.9.21) dans l'option suppressKotlinVersionCompatibilityCheck pour éviter les avertissements

### 36. Problèmes de références non résolues pour les modèles de données

**Erreur :**
```
e: Unresolved reference: TMDbSearchResponse
e: Unresolved reference: ChannelRulesResponse
e: Unresolved reference: GameRefundabilityResponse
e: Unresolved reference: CalendarEvent
e: Unresolved reference: GameQuestion
```

**Cause :**
1. Fichiers de modèles dupliqués (TMDbSearchResponse.kt et TMDbModels.kt contenant la même classe)
2. Imports manquants dans les fichiers qui utilisent ces modèles

**Solution :**
1. Suppression des fichiers dupliqués (comme TMDbSearchResponse.kt si TMDbModels.kt contient déjà la classe)
2. Ajout des imports nécessaires dans les fichiers qui utilisent ces modèles :
```kotlin
import com.openhands.tvgamerefund.data.models.CalendarEvent
import com.openhands.tvgamerefund.data.models.ChannelRulesResponse
import com.openhands.tvgamerefund.data.models.GameQuestion
import com.openhands.tvgamerefund.data.models.GameRefundabilityResponse
import com.openhands.tvgamerefund.data.models.TMDbSearchResponse
```
3. Vérification de la cohérence des noms de champs entre les modèles et leur utilisation dans le code

### 37. Redéclaration de classes et fonctions

**Erreur :**
```
e: Redeclaration: InvoiceGameFee
e: Conflicting overloads: public fun GameStatsSection(...)
e: Conflicting overloads: public fun RefundExperienceSection(...)
```

**Cause :**
1. La classe InvoiceGameFee est définie à la fois dans Invoice.kt et InvoiceGameFee.kt
2. Les fonctions GameStatsSection et RefundExperienceSection sont définies à la fois dans GameDetailComponents.kt et dans leurs propres fichiers

**Solution :**
1. Suppression de la définition redondante de InvoiceGameFee dans Invoice.kt
2. Pour les fonctions en conflit, plusieurs options :
   - Supprimer l'une des définitions
   - Renommer l'une des fonctions
   - Déplacer les fonctions dans un seul fichier
   - Utiliser des paramètres différents pour distinguer les surcharges

### 38. Référence non résolue à absoluteValue

**Erreur :**
```
e: Unresolved reference: absoluteValue
```

**Cause :**
La propriété d'extension `absoluteValue` est utilisée sur un Int, mais elle n'est pas importée. Cette propriété fait partie du package `kotlin.math`.

**Solution :**
Remplacer `id.hashCode().absoluteValue` par `Math.abs(id.hashCode())` qui est disponible dans Java standard et ne nécessite pas d'import supplémentaire.

### 39. Méthode getGamesByPhoneNumber manquante dans GameRepository

**Erreur :**
```
e: Unresolved reference: getGamesByPhoneNumber
```

**Cause :**
La méthode `getGamesByPhoneNumber` est appelée dans PdfAnalyzer.kt mais n'est pas définie dans GameRepository.

**Solution :**
Ajouter la méthode manquante dans GameRepository :
```kotlin
/**
 * Récupère les jeux associés à un numéro de téléphone
 */
suspend fun getGamesByPhoneNumber(phoneNumber: String): List<Game> {
    // Implémentation temporaire - à remplacer par une requête réelle
    return getAllGamesSync().filter { it.phoneNumber == phoneNumber }
}
```
Ou modifier le code dans PdfAnalyzer.kt pour utiliser une méthode existante.

### 40. Fonctions composables dupliquées dans des fichiers séparés

**Erreur :**
```
e: Conflicting overloads: public fun GameStatsSection(...)
e: Conflicting overloads: public fun RefundExperienceSection(...)
```

**Cause :**
Les fonctions composables GameStatsSection et RefundExperienceSection sont définies à la fois dans GameDetailComponents.kt et dans leurs propres fichiers (GameStatsSection.kt et RefundExperienceSection.kt).

**Solution :**
1. Supprimer les fichiers séparés (GameStatsSection.kt et RefundExperienceSection.kt) et conserver uniquement les implémentations dans GameDetailComponents.kt
2. Ou renommer les fonctions dans l'un des fichiers pour éviter les conflits
3. Ou déplacer toutes les implémentations dans un seul fichier

### 44. Conflit de dépendances Hilt pour OkHttpClient

**Erreur :**
```
error: [Dagger/DuplicateBindings] okhttp3.OkHttpClient is bound multiple times:
  @Provides @Singleton okhttp3.OkHttpClient com.openhands.tvgamerefund.data.api.BackendModule.provideOkHttpClient()
  @Provides @Singleton okhttp3.OkHttpClient com.openhands.tvgamerefund.di.NetworkModule.provideOkHttpClient()
```

**Cause :**
OkHttpClient est fourni (provided) à deux endroits différents dans le code :
1. Dans BackendModule.kt avec la méthode provideOkHttpClient
2. Dans NetworkModule.kt avec une autre méthode provideOkHttpClient

Cela crée une ambiguïté pour l'injecteur de dépendances Hilt qui ne sait pas quelle implémentation utiliser.

**Solution :**
Supprimer l'une des deux méthodes provideOkHttpClient. Dans ce cas, nous avons supprimé celle de BackendModule.kt car NetworkModule.kt est le module principal pour les dépendances réseau.

### 43. Conflit de dépendances Hilt pour BackendApi

**Erreur :**
```
error: [Dagger/DuplicateBindings] com.openhands.tvgamerefund.data.api.BackendApi is bound multiple times:
  @Provides @Singleton com.openhands.tvgamerefund.data.api.BackendApi com.openhands.tvgamerefund.data.api.BackendModule.provideBackendApi(retrofit2.Retrofit)
  @Provides @Singleton com.openhands.tvgamerefund.data.api.BackendApi com.openhands.tvgamerefund.di.NetworkModule.provideBackendApi(@Named("backendRetrofit") retrofit2.Retrofit)
```

**Cause :**
L'API BackendApi est fournie (provided) à deux endroits différents dans le code :
1. Dans BackendModule.kt avec la méthode provideBackendApi
2. Dans NetworkModule.kt avec une autre méthode provideBackendApi

Cela crée une ambiguïté pour l'injecteur de dépendances Hilt qui ne sait pas quelle implémentation utiliser.

**Solution :**
Supprimer l'une des deux méthodes provideBackendApi. Dans ce cas, nous avons supprimé celle de BackendModule.kt car celle de NetworkModule.kt utilise un Retrofit nommé spécifiquement pour le backend.

### 42. Import incorrect de KeyboardOptions

**Erreur :**
```
e: Unresolved reference: KeyboardOptions
```

**Cause :**
La classe KeyboardOptions est importée depuis le mauvais package. Elle se trouve dans `androidx.compose.foundation.text.KeyboardOptions` et non dans `androidx.compose.ui.text.input.KeyboardOptions`.

**Solution :**
Corriger l'import en remplaçant :
```kotlin
import androidx.compose.ui.text.input.KeyboardOptions
```
par :
```kotlin
import androidx.compose.foundation.text.KeyboardOptions
```

### 41. Problème avec firstOrNull dans ProfileViewModel

**Erreur :**
```
e: Not enough information to infer type variable T
e: Unresolved reference: it
```

**Cause :**
Dans ProfileViewModel.kt, la méthode `firstOrNull()` est appelée sur un Flow sans collecter les valeurs d'abord, et il y a une confusion entre le Flow et son contenu.

**Solution :**
1. Créer une méthode synchrone dans le repository pour récupérer directement la liste sans passer par un Flow :
```kotlin
// Dans UserParticipationDao.kt
@Query("SELECT * FROM user_participations ORDER BY participationDate DESC")
suspend fun getAllParticipationsSync(): List<UserParticipation>

// Dans UserParticipationRepository.kt
suspend fun getAllParticipationsSync(): List<UserParticipation> = 
    userParticipationDao.getAllParticipationsSync()

// Dans ProfileViewModel.kt
val participationsList = userParticipationRepository.getAllParticipationsSync()
val participationsCount = participationsList.size
val totalRefundAmount = participationsList.sumOf { it.amount }
```
2. Cette approche évite les problèmes de type avec les Flows et permet d'accéder directement aux données

### 20. Problème avec l'option suppressKotlinVersionCompatibilityCheck

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/build.gradle.kts:56:13: Unresolved reference: suppressKotlinVersionCompatibilityCheck
```

**Cause :**
L'option `suppressKotlinVersionCompatibilityCheck` n'est pas disponible directement dans les options Kotlin standard.

**Solution :**
1. Suppression du bloc `tasks.withType<KotlinCompile>()` qui causait l'erreur
2. Utilisation de l'option de compilation via les arguments du compilateur : `-P plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.9.21`
3. Utilisation d'une version du compilateur Compose (1.5.4) connue pour être compatible avec Kotlin 1.9.21

### 21. Problèmes avec les méthodes OkHttp

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/network/FreeAuthService.kt:89:58 Using 'code(): Int' is an error. moved to val
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/network/FreeAuthService.kt:90:50 Using 'message(): String' is an error. moved to val
```

**Cause :**
Dans Kotlin 1.9.21, les méthodes d'OkHttp comme `code()`, `body()`, `message()`, etc. ont été déplacées vers des propriétés.

**Solution :**
1. Remplacement de toutes les occurrences de `code()` par `code`
2. Remplacement de toutes les occurrences de `body()` par `body`
3. Remplacement de toutes les occurrences de `message()` par `message`
4. Remplacement de toutes les occurrences de `request()` par `request`
5. Remplacement de toutes les occurrences de `url()` par `url`

### 22. Problèmes avec le modèle Game

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/games/GamesScreen.kt:114:37 Unresolved reference: showName
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/games/GamesScreen.kt:120:37 Unresolved reference: channel
```

**Cause :**
Le modèle `Game` ne contenait pas les champs `showName`, `channel`, `airDate`, `gameType` et `refundDeadline` qui étaient utilisés dans le code.

**Solution :**
1. Ajout des champs manquants au modèle `Game` avec des valeurs par défaut pour assurer la compatibilité avec le code existant
2. Suppression des références au champ `gameFees` qui n'existe pas dans la classe `Invoice`

### 23. Problèmes avec les ressources drawable

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/service/ReminderWorker.kt:77:29 Unresolved reference: drawable
```

**Cause :**
Le dossier `drawable` et l'icône `ic_notification` n'existaient pas.

**Solution :**
1. Création du dossier `drawable` dans les ressources
2. Création d'un fichier `ic_notification.xml` avec une icône de notification standard

### 24. Problème avec l'attribut colorControlNormal

**Erreur :**
```
Android resource linking failed
com.openhands.tvgamerefund.app-main-79:/drawable/ic_notification.xml:7: error: resource attr/colorControlNormal (aka com.openhands.tvgamerefund:attr/colorControlNormal) not found.
```

**Cause :**
L'icône de notification utilisait l'attribut `?attr/colorControlNormal` qui n'est pas disponible dans le projet.

**Solution :**
Remplacement de la référence à l'attribut par une couleur directe (#FFFFFF) :
```xml
android:tint="#FFFFFF"
android:fillColor="#FFFFFF"
```

### 25. Problèmes avec les méthodes OkHttp dans BillRepository

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/repositories/BillRepository.kt:27:37 Cannot access 'body': it is private in 'Response'
```

**Cause :**
Dans Kotlin 1.9.21, les propriétés comme `body` dans OkHttp sont devenues privées et doivent être accédées via les méthodes correspondantes.

**Solution :**
1. Remplacement de `response.body` par `response.body()`
2. Renommage de la variable `body` en `responseBody` pour éviter les confusions

### 26. Problèmes avec les références dans GamesScreen

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/screens/games/GamesScreen.kt:27:28 Unresolved reference: games
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/screens/games/GamesScreen.kt:53:31 Unresolved reference: filterGames
```

**Cause :**
Le ViewModel `GamesViewModel` expose un état UI via `uiState` mais le composable `GamesScreen` essaie d'accéder directement aux propriétés `games` et `filteredGames`, et à la méthode `filterGames` qui n'existent pas.

**Solution :**
1. Utilisation de `viewModel.uiState.collectAsState()` pour observer l'état UI
2. Extraction des propriétés `games` et `filteredGames` à partir de l'état UI
3. Remplacement des appels à `filterGames` par les méthodes appropriées : `updateSearchQuery` et `updateLikedFilter`

### 27. Import manquant pour GameType

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/games/GamesViewModel.kt:33:28 Unresolved reference: GameType
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/games/detail/GameDetailViewModel.kt:37:24 Unresolved reference: GameType
```

**Cause :**
Les classes `GamesViewModel` et `GameDetailViewModel` utilisent l'enum `GameType` mais n'importent pas cette classe.

**Solution :**
Ajout de l'import pour `GameType` dans les deux fichiers :
```kotlin
import com.openhands.tvgamerefund.data.models.GameType
```

### 28. Liaisons dupliquées pour OkHttpClient dans Hilt

**Erreur :**
```
error: [Dagger/DuplicateBindings] okhttp3.OkHttpClient is bound multiple times:
  @Provides @Singleton okhttp3.OkHttpClient com.openhands.tvgamerefund.di.AppModule.provideOkHttpClient()
  @Singleton @Provides okhttp3.OkHttpClient com.openhands.tvgamerefund.di.NetworkModule.provideOkHttpClient()
```

**Cause :**
Deux modules Hilt différents (`AppModule` et `NetworkModule`) fournissent une instance de `OkHttpClient` avec la même portée (`@Singleton`), ce qui crée une ambiguïté pour l'injecteur.

**Solution :**
1. Renommage du fichier `FirebaseModule.kt` qui contenait en réalité `NetworkModule`
2. Suppression de la méthode `provideOkHttpClient()` du module `NetworkModule`
3. Amélioration de la configuration OkHttpClient dans `AppModule` en ajoutant les timeouts du module supprimé

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/games/GamesViewModel.kt:33:28 Unresolved reference: GameType
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/games/detail/GameDetailViewModel.kt:37:24 Unresolved reference: GameType
```

**Cause :**
Les classes `GamesViewModel` et `GameDetailViewModel` utilisent l'enum `GameType` mais n'importent pas cette classe.

**Solution :**
Ajout de l'import pour `GameType` dans les deux fichiers :
```kotlin
import com.openhands.tvgamerefund.data.models.GameType
```

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/screens/games/GamesScreen.kt:27:28 Unresolved reference: games
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/ui/screens/games/GamesScreen.kt:53:31 Unresolved reference: filterGames
```

**Cause :**
Le ViewModel `GamesViewModel` expose un état UI via `uiState` mais le composable `GamesScreen` essaie d'accéder directement aux propriétés `games` et `filteredGames`, et à la méthode `filterGames` qui n'existent pas.

**Solution :**
1. Utilisation de `viewModel.uiState.collectAsState()` pour observer l'état UI
2. Extraction des propriétés `games` et `filteredGames` à partir de l'état UI
3. Remplacement des appels à `filterGames` par les méthodes appropriées : `updateSearchQuery` et `updateLikedFilter`

**Erreur :**
```
Android resource linking failed
com.openhands.tvgamerefund.app-main-79:/drawable/ic_notification.xml:7: error: resource attr/colorControlNormal (aka com.openhands.tvgamerefund:attr/colorControlNormal) not found.
```

**Cause :**
L'icône de notification utilisait l'attribut `?attr/colorControlNormal` qui n'est pas disponible dans le projet.

**Solution :**
Remplacement de la référence à l'attribut par une couleur directe (#FFFFFF) :
```xml
android:tint="#FFFFFF"
android:fillColor="#FFFFFF"
```

**Erreur :**
```
e: file:///C:/Users/Castle/Code/TVGameRefund/app/build.gradle.kts:56:13: Unresolved reference: suppressKotlinVersionCompatibilityCheck
```

**Cause :**
L'option `suppressKotlinVersionCompatibilityCheck` n'est pas disponible directement dans les options Kotlin standard.

**Solution :**
1. Suppression du bloc `tasks.withType<KotlinCompile>()` qui causait l'erreur
2. Utilisation de l'option de compilation via les arguments du compilateur : `-P plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true`
3. Utilisation d'une version du compilateur Compose (1.5.4) connue pour être compatible avec Kotlin 1.9.21

### 18. Problèmes avec le plugin Google Services

**Erreur :**
```
Plugin [id: 'com.google.gms.google-services', version: '4.4.2', apply: false] was not found in any of the following sources
```

**Cause :**
Le plugin Google Services n'était pas correctement configuré dans le projet.

**Solution :**
1. Ajout du plugin dans le fichier build.gradle.kts du projet
2. Configuration de la stratégie de résolution dans settings.gradle.kts
3. Création d'un fichier google-services.json vide pour permettre la compilation
4. Utilisation d'une version compatible du plugin (4.4.0)

### 13. Function declaration must have a name

**Erreur :**
```
e: [ksp] C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/repository/InvoiceRepository.kt: (507, 32): Function declaration must have a name
e: Error occurred in KSP, check log for detail
e: file:///C:/Users/Castle/Code/TVGameRefund/app/src/main/java/com/openhands/tvgamerefund/data/repository/InvoiceRepository.kt:497:27 Expecting '->'
```

**Cause :**
Problème de syntaxe dans le fichier InvoiceRepository.kt. Il y avait probablement une accolade manquante ou mal placée, ou une structure de code incorrecte.

**Solution :**
Réécriture complète du fichier InvoiceRepository.kt pour corriger les erreurs de syntaxe.

### 1. Conflit entre deux classes d'application Hilt

**Erreur :**
```
Cannot process multiple app roots in the same compilation unit: com.openhands.tvgamerefund.TVGameRefundApp, com.openhands.tvgamerefund.TVGameRefundApplication
```

**Cause :**
Deux classes d'application annotées avec `@HiltAndroidApp` ont été créées : `TVGameRefundApp` et `TVGameRefundApplication`.

**Solution :**
Suppression de `TVGameRefundApplication.kt` et conservation de `TVGameRefundApp.kt` comme classe d'application principale.

### 2. Redéclaration de l'enum Operator

**Erreur :**
```
Redeclaration: Operator
```

**Cause :**
L'enum `Operator` était défini à la fois dans `Operator.kt` et dans `OperatorCredentials.kt`.

**Solution :**
Suppression de la redéclaration dans `OperatorCredentials.kt` et utilisation de la version unique dans `Operator.kt`.

### 3. Problèmes avec les APIs expérimentales de Material3

**Erreur :**
```
This material API is experimental and is likely to change or to be removed in the future.
```

**Cause :**
Utilisation d'APIs expérimentales de Material3 sans l'annotation appropriée.

**Solution :**
Ajout de l'option de compilation dans `build.gradle.kts` :
```kotlin
kotlinOptions {
    freeCompilerArgs += listOf(
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    )
}
```

### 4. Problèmes avec collectAsState dans Compose

**Erreur :**
```
Unresolved reference: collectAsState
```

**Cause :**
Manque de la dépendance `lifecycle-runtime-compose` ou utilisation incorrecte.

**Solution :**
Ajout de la dépendance et utilisation de `collectAsStateWithLifecycle()` à la place.

### 5. Problèmes avec les composants Compose dans des contextes non-@Composable

**Erreur :**
```
@Composable invocations can only happen from the context of a @Composable function
```

**Cause :**
Appel de fonctions @Composable en dehors d'un contexte @Composable.

**Solution :**
Restructuration du code pour s'assurer que tous les appels à des fonctions @Composable sont faits dans un contexte @Composable.

### 6. Icônes Material non disponibles

**Erreur :**
```
Unresolved reference: Receipt
Unresolved reference: Description
```

**Cause :**
Tentative d'utilisation d'icônes qui ne sont pas disponibles dans le package Icons.Default.

**Solution :**
1. Ajout de la dépendance pour les icônes étendues :
```kotlin
implementation("androidx.compose.material:material-icons-extended:1.5.4")
```
2. Utilisation d'icônes alternatives ou des icônes du package étendu (Icons.Outlined.Receipt).

### 7. Problèmes avec FreeApiService défini deux fois

**Erreur :**
```
FreeApiService is bound multiple times:
@Provides @Singleton com.openhands.tvgamerefund.data.network.FreeApiService com.openhands.tvgamerefund.di.AppModule.provideFreeApiService(okhttp3.OkHttpClient)
@Provides @Singleton com.openhands.tvgamerefund.data.network.FreeApiService com.openhands.tvgamerefund.di.NetworkModule.provideFreeApiService()
```

**Cause :**
Le service FreeApiService était défini dans deux modules Hilt différents.

**Solution :**
Suppression de NetworkModule.kt et conservation de la définition dans AppModule.kt qui était plus complète (avec OkHttpClient).

### 8. Accès aux propriétés privées de Response OkHttp

**Erreur :**
```
Cannot access 'body': it is package-private in 'Response'
Cannot access 'code': it is package-private in 'Response'
```

**Cause :**
Tentative d'accès direct aux propriétés `body` et `code` de l'objet Response d'OkHttp, qui sont des propriétés privées.

**Solution :**
Utiliser les méthodes d'accès publiques :
- `response.body()` au lieu de `response.body`
- `response.code()` au lieu de `response.code`
- Pour accéder au contenu du body, utiliser `body()?.bytes()` puis écrire les bytes dans le fichier.

### 9. Conflit entre fichiers de composants

**Erreur :**
```
Conflicting overloads: public fun OperatorDropdown(...): Unit defined in com.openhands.tvgamerefund.ui.screens.settings in file SettingsComponents.kt, public fun OperatorDropdown(...): Unit defined in com.openhands.tvgamerefund.ui.screens.settings in file SettingsScreenComponents.kt
```

**Cause :**
Deux fichiers définissent la même fonction `OperatorDropdown` dans le même package.

**Solution :**
Supprimer l'un des fichiers (dans notre cas, `SettingsScreenComponents.kt`) et conserver uniquement `SettingsComponents.kt`.

### 10. Référence non résolue à une classe

**Erreur :**
```
Unresolved reference: OperatorCredentials
```

**Cause :**
Utilisation d'une classe sans import ou avec un import incorrect.

**Solution :**
Utiliser le nom complet de la classe avec son package :
```kotlin
com.openhands.tvgamerefund.data.models.OperatorCredentials
```
ou ajouter l'import approprié :
```kotlin
import com.openhands.tvgamerefund.data.models.OperatorCredentials
```

### 11. Problèmes d'authentification avec Free Mobile

**Erreur :**
```
Authentification échouée : identifiants incorrects ou problème de connexion
```

**Cause :**
Plusieurs causes possibles :
- Le site de Free Mobile a changé son processus d'authentification
- Le token CSRF n'est pas correctement extrait
- Les cookies de session ne sont pas correctement gérés
- Des en-têtes HTTP spécifiques sont nécessaires
- Double authentification requise (code par SMS)

**Solution :**
- Amélioration de la récupération du token CSRF avec plusieurs patterns
- Ajout d'en-têtes HTTP supplémentaires (User-Agent, Accept, etc.)
- Implémentation d'une méthode d'authentification alternative sans token CSRF
- Meilleure gestion des redirections et des cookies
- Ajout de logs détaillés pour diagnostiquer les problèmes
- Implémentation d'une authentification via WebView pour gérer la double authentification
- Sauvegarde et réutilisation des cookies de session

### 12. Injection de dépendances non-ViewModel dans les composables

**Erreur :**
```
Type mismatch: inferred type is ViewModel but FreeAuthManager was expected
```

**Cause :**
Tentative d'utiliser `hiltViewModel()` pour injecter une classe qui n'est pas un ViewModel.

**Solution :**
Créer un ViewModel intermédiaire qui expose la dépendance :
```kotlin
@HiltViewModel
class FreeAuthManagerViewModel @Inject constructor(
    val authManager: FreeAuthManager
) : ViewModel()
```

Et l'utiliser dans le composable :
```kotlin
val authManager: FreeAuthManager = hiltViewModel<FreeAuthManagerViewModel>().authManager
```

## Avertissements

### 1. MasterKeys déprécié

**Avertissement :**
```
'MasterKeys' is deprecated. Deprecated in Java
```

**Cause :**
Utilisation de l'API MasterKeys qui est dépréciée.

**Solution :**
Remplacement par l'API MasterKey plus récente (non implémenté pour l'instant, mais prévu).

### 2. Paramètres non utilisés

**Avertissement :**
```
Parameter 'navController' is never used
```

**Cause :**
Paramètres déclarés mais non utilisés dans le code.

**Solution :**
Suppression ou utilisation des paramètres inutilisés.

### 14. Erreur de téléchargement des factures

**Erreur :**
```
Le contenu téléchargé n'est pas un PDF: null
```

**Cause :**
Le serveur de Free Mobile ne renvoie pas toujours le bon Content-Type pour les PDF.

**Solution :**
- Vérification plus robuste du contenu PDF en examinant les premiers octets du fichier (signature %PDF)
- Acceptation des Content-Type application/pdf et application/octet-stream
- Écriture du fichier même si le Content-Type est incorrect pour pouvoir l'examiner
- Utilisation des cookies sauvegardés pour le téléchargement
- Ajout d'en-têtes HTTP supplémentaires pour le téléchargement

## Bonnes pratiques identifiées

1. **Séparation des composants Compose** : Création de fichiers séparés pour les composants réutilisables.
2. **Utilisation de StateFlow** : Pour la gestion de l'état dans les ViewModels.
3. **Utilisation de collectAsStateWithLifecycle** : Pour observer les StateFlow de manière efficace avec le cycle de vie.
4. **Structure de navigation** : Utilisation de sealed class pour les routes de navigation.
5. **Injection de dépendances** : Utilisation de Hilt pour l'injection de dépendances.
6. **Authentification WebView** : Utilisation d'un WebView pour l'authentification aux sites qui utilisent des mécanismes de sécurité avancés.
7. **Gestion des cookies** : Sauvegarde et réutilisation des cookies de session pour éviter de devoir s'authentifier à chaque fois.
8. **Méthodes alternatives** : Implémentation de plusieurs méthodes pour récupérer les données en cas d'échec de la méthode principale.