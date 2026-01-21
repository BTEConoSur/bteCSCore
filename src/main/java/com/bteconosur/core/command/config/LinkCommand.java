package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlaceholderUtil;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.discord.util.LinkService;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class LinkCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public LinkCommand() {
        super("link", "Linkear la cuenta de Discord.", "<código>|[subcomando]");
        //this.addSubcommand(new LinkSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 0) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        if (LinkService.isPlayerLinked(player)) {
            PlayerLogger.error(sender, lang.getString("minecraft-already-linked"), (String) null);
            return true;
        }

        if (args.length == 0) {
            String code;
            if (LinkService.hasMinecraftCode(player)) code = LinkService.getMinecraftCode(player);
            else code = LinkService.generateMinecraftCode(player);
            TagResolver resolver = PlaceholderUtil.getClickableText("codigo", code, code);
            PlayerLogger.info(sender, lang.getString("minecraft-code").replace("%codigo%", code), (String) null, resolver);
            return true;
        }
        
        String code = args[0];
        if (!LinkService.isDiscordCodeValid(code)) {
            PlayerLogger.error(sender, lang.getString("invalid-link-code"), (String) null);
            return true;
        }
        
        LinkService.linkMinecraft(code, player);
        PlayerLogger.info(sender, lang.getString("minecraft-link-success"), ChatUtil.getDsLinkSuccess(player.getNombre()));
        return true;
    }

}
