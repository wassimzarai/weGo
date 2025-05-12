# Documentation de l'API de Réservation Automatique

## API de Réservation Automatique

Cette API permet à un utilisateur possédant un abonnement actif de créer automatiquement une réservation sur le prochain trajet disponible. L'algorithme sélectionne un trajet correspondant à ses préférences (itinéraire habituel ou spécifié) et valide la réservation si l'abonnement est encore valide.

### Endpoint

```
POST /api/reservations/automatique
```

### Format de la requête

```json
{
  "passagerId": 123,
  "villeDepart": "Tunis",
  "villeArrivee": "Sfax",
  "nombrePlaces": 2,
  "utiliserItineraireHabituel": false
}
```

| Paramètre | Type | Description |
|-----------|------|-------------|
| passagerId | integer | ID de l'utilisateur qui fait la demande |
| villeDepart | string | Ville de départ (ignoré si utiliserItineraireHabituel=true) |
| villeArrivee | string | Ville d'arrivée (ignoré si utiliserItineraireHabituel=true) |
| nombrePlaces | integer | Nombre de places à réserver |
| utiliserItineraireHabituel | boolean | Si true, utilise l'itinéraire le plus fréquent de l'utilisateur |

### Format de la réponse

```json
{
  "message": "Votre réservation pour le trajet de 08h30 a été confirmée avec succès.",
  "data": { ... détails de la réservation ... },
  "success": true,
  "statusCode": 201
}
```

### Exemple de messages de réponse

#### Succès (201)
- "Votre réservation pour le trajet de 08h30 a été confirmée avec succès."

#### Erreurs
- 400: "Réservation impossible : votre abonnement est expiré. Veuillez le renouveler pour continuer."
- 400: "Réservation impossible : vous n'avez aucune réservation associée à votre compte."
- 400: "Réservation impossible : aucun itinéraire habituel trouvé. Veuillez spécifier l'itinéraire souhaité."
- 400: "Réservation impossible : veuillez spécifier les villes de départ et d'arrivée."
- 404: "Impossible de créer la réservation : compte utilisateur introuvable."
- 404: "Réservation impossible : aucun trajet disponible ne correspond à votre demande dans les prochains jours."
- 500: "Une erreur est survenue lors de la création de votre réservation. Veuillez réessayer plus tard." 