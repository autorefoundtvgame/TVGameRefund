# Journal des erreurs et solutions

Ce document recense les erreurs rencontrées pendant le développement de l'application TVGameRefund et les solutions mises en place.

## Erreurs de compilation

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

## Bonnes pratiques identifiées

1. **Séparation des composants Compose** : Création de fichiers séparés pour les composants réutilisables.
2. **Utilisation de StateFlow** : Pour la gestion de l'état dans les ViewModels.
3. **Utilisation de collectAsStateWithLifecycle** : Pour observer les StateFlow de manière efficace avec le cycle de vie.
4. **Structure de navigation** : Utilisation de sealed class pour les routes de navigation.
5. **Injection de dépendances** : Utilisation de Hilt pour l'injection de dépendances.