package com.iditechs.filmu

object FilmuErrors {
    fun connectionFailed(): String =
        "Impossible de se connecter au serveur. Vérifiez l'adresse et votre connexion internet."

    fun authFailed(): String =
        "Identifiants incorrects. Vérifiez le nom d'utilisateur et le mot de passe."

    fun subscriptionExpired(): String =
        "Votre abonnement a expiré. Contactez votre fournisseur pour le renouveler."

    fun serverUnavailable(): String =
        "Le serveur est temporairement indisponible. Réessayez plus tard."

    fun streamFailed(): String =
        "Impossible de lire ce flux. Essayez un autre serveur ou canal."

    fun timeout(): String =
        "La connexion a pris trop de temps. Vérifiez votre réseau."

    fun forStatus(status: String?): String? = when (status) {
        "Active" -> null
        "Expired" -> subscriptionExpired()
        "Disabled", "Banned" -> "Ce compte a été désactivé. Contactez votre fournisseur."
        else -> null
    }
}
