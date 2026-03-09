package com.bteconosur.core.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.scoreboard.ScoreboardManager;
import com.bteconosur.core.tab.TabManager;
import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.util.ConfigurationService;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.discord.DiscordManager;

public class PlayerJoinListener implements Listener {

    private final PlayerRegistry playerRegistry;
    private final TipoUsuarioRegistry tipoUsuarioRegistry;
    private final RangoUsuarioRegistry rangoUsuarioRegistry;
    private final PermissionManager permissionManager;

    public PlayerJoinListener() {
        playerRegistry = PlayerRegistry.getInstance();
        tipoUsuarioRegistry = TipoUsuarioRegistry.getInstance();
        rangoUsuarioRegistry = RangoUsuarioRegistry.getInstance();
        permissionManager = PermissionManager.getInstance();
    }

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    private static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    private static final Set<UUID> newPlayers = new HashSet<>();

    /**
     * Procesa el estado de descarga del resource pack del jugador.
     * Si el jugador es nuevo y la carga fue exitosa, determina su idioma por defecto,
     * notifica fallback cuando corresponde y envía el mensaje de bienvenida.
     *
     * @param event evento con el estado del resource pack del jugador.
     */
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        org.bukkit.entity.Player bukkitPlayer = event.getPlayer();
        Player player = playerRegistry.get(event.getPlayer().getUniqueId());
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                if (newPlayers.contains(event.getPlayer().getUniqueId())) {
                    Language language = LanguageHandler.checkDefaultLang(event.getPlayer());

                    if (language == null) {
                        language = Language.getInternationalDefault();
                        if (player == null) PlayerLogger.sendMc(LanguageHandler.getText(language, "language.not-supported").replace("%language%", event.getPlayer().locale().toLanguageTag()), bukkitPlayer);
                        else PlayerLogger.warn(player, LanguageHandler.getText(language, "language.not-supported").replace("%language%", event.getPlayer().locale().toLanguageTag()), (String) null);
                    } else {
                        if (player == null) PlayerLogger.sendMc(LanguageHandler.getText(language, "language.default").replace("%language%", LanguageHandler.getText(language, "placeholder.lang-mc." + language.getCode())), bukkitPlayer);
                        else PlayerLogger.info(player, LanguageHandler.getText(language, "language.default").replace("%language%", LanguageHandler.getText(language, "placeholder.lang-mc." + language.getCode())), (String) null);
                    }
                    List<String> processed = new ArrayList<>();
                    for (String line : LanguageHandler.getTextList(language, "player-welcome-message")) {
                    processed.add(line.replace("%player%", event.getPlayer().getName()));
                    }
                    if (player != null) PlayerLogger.sendMc(String.join("\n", processed), bukkitPlayer);
                    else PlayerLogger.send(player, String.join("\n", processed), (String) null);
                    newPlayers.remove(bukkitPlayer.getUniqueId());
                }
                
                break;
            default:
                break;
        }
    }

    /**
     * Gestiona el ingreso del jugador al servidor.
     * Registra jugadores nuevos, actualiza jugadores existentes, aplica configuraciones
     * iniciales de chat y scoreboard, y sincroniza permisos y actividad externa.
     *
     * @param event evento de ingreso del jugador.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        DiscordManager.getInstance().updateActivity(false);
        org.bukkit.entity.Player bukkitPlayer = event.getPlayer();
        Player player;
        Language language = LanguageHandler.checkDefaultLang(event.getPlayer());
        bukkitPlayer.setResourcePack(secret.getString("rp-url"), secret.getString("rp-sha1"), config.getBoolean("rp-required"));
        if (!playerRegistry.exists(event.getPlayer().getUniqueId())) {
            player = new Player(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                new Date(),
                tipoUsuarioRegistry.getVisita(),
                rangoUsuarioRegistry.getNormal()
            );
            player.setFechaUltimaConexion(new Date());
            player.setConfiguration(new Configuration(player));
            
            if (language == null) language = Language.getInternationalDefault();

            player.getConfiguration().setLang(language);
            playerRegistry.load(player);
            player = ConfigurationService.setDefaults(player);
            
            GlobalChatService.broadcastNewPlayerJoinedServer(player);
            newPlayers.add(event.getPlayer().getUniqueId());
            TabManager.getInstance().joinPlayer(player);
        } else {
            player = playerRegistry.get(event.getPlayer().getUniqueId());
            player.setNombre(event.getPlayer().getName());
            player.setFechaUltimaConexion(new Date());
            player = playerRegistry.merge(player.getUuid());
            GlobalChatService.broadcastPlayerJoinedServer(player);
        }
        if (player.getConfiguration().getGeneralScoreboard()) ScoreboardManager.getInstance().addPlayer(player);
        
        if (player.getConfiguration().getGeneralGlobalChatOnJoin()) ChatService.setChatToGlobal(player);
        else if (ChatService.wasInCountryChat(player)) ChatService.setChatToCountry(player);
        else if (ChatService.isInNotePad(player)) ChatService.setChatToNotePad(player);
        else ChatService.setChatToGlobal(player);
        
        permissionManager.checkTipoUsuario(player);
        permissionManager.checkRangoUsuario(player);
    }
}
