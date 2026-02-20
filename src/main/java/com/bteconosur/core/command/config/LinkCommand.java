package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.discord.util.LinkService;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class LinkCommand extends BaseCommand {

    public LinkCommand() {
        super("link", "Linkear la cuenta de Discord.", "<código>|[subcomando]", "btecs.command.link");
        this.addSubcommand(new LinkSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        if (args.length != 1 && args.length != 0) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        if (LinkService.isPlayerLinked(player)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "link.mc-already-linked"), (String) null);
            return true;
        }

        if (args.length == 0) {
            String code;
            if (LinkService.hasMinecraftCode(player)) code = LinkService.getMinecraftCode(player);
            else code = LinkService.generateMinecraftCode(player);
            TagResolver resolver = TagResolverUtils.getCopyableText("codigo", code, code, player.getLanguage());
            PlayerLogger.info(sender, LanguageHandler.getText(language, "link.mc-code").replace("%codigo%", code), (String) null, resolver);
            return true;
        }
        
        String code = args[0];
        if (!LinkService.isDiscordCodeValid(code)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "link.invalid-code"), (String) null);
            return true;
        }
        
        LinkService.linkMinecraft(code, player);
        PlayerLogger.info(sender, LanguageHandler.getText(language, "link.mc-success"), ChatUtil.getDsLinkSuccess(player));
        return true;
    }

}
