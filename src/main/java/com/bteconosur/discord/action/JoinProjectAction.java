package com.bteconosur.discord.action;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class JoinProjectAction implements ButtonAction {

    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    @SuppressWarnings("null")
    @Override
    public void handle(ButtonInteractionEvent event, Interaction ctx) {
        String buttonId = event.getComponentId();
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        if (player == null) {
            event.reply(lang.getString("discord-link-needed")).setEphemeral(true).queue();
            return;
        }
        Proyecto proyecto = ProyectoRegistry.getInstance().get(ctx.getProjectId());
        if (!PermissionManager.getInstance().isLider(player, proyecto)) {
            event.reply(lang.getString("not-a-leader-project").replace("%proyectoId%", proyecto.getId())).setEphemeral(true).queue();
            return;
        }

        ProjectManager pm = ProjectManager.getInstance();
        if (buttonId.equals("accept")) {
            pm.acceptJoinRequest(proyecto.getId(), ctx.getPlayerId(), ctx.getId(), player.getUuid());
            event.reply(lang.getString("ds-join-accepted")).queue();
        } else if (buttonId.equals("cancel")) {
            pm.rejectJoinRequest(proyecto.getId(), ctx.getPlayerId(), ctx.getId(), player.getUuid());
            event.reply(lang.getString("ds-join-rejected")).queue();
        } else {
            event.reply(lang.getString("discord-invalid-action")).setEphemeral(true).queue();
        }
    }

}
