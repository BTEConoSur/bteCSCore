package com.bteconosur.discord.util;

/**
 * Enumeración de modos de registro para comandos de Discord.
 * Define dónde se registrará un comando (globalmente, en servidores de países, staffhub o combinaciones).
 */
public enum CommandMode {
    /** Comando disponible globalmente en todos los servidores y DM*/
    GLOBAL,
    /** Comando disponible únicamente en servidores de países */
    COUNTRY_ONLY,
    /** Comando disponible únicamente en el servidor staffhub */
    STAFFHUB_ONLY,
    /** Comando disponible en servidores de países y staffhub */
    COUNTRY_AND_STAFFHUB;
}
