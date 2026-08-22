package com.bteconosur.core.command;

public class AssetsCommand extends BaseCommand {

    public AssetsCommand() {
        super("assets", "", "btecs.command.assets", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        //PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "assets-teleport"), (String) null);
        bukkitPlayer.performCommand("essentials:warp assets");
        return true;
    }

}
