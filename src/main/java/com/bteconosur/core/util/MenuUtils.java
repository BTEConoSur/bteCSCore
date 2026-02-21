package com.bteconosur.core.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.util.PlaceholderUtils;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIFinishHandler;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MenuUtils {

    private static final YamlConfiguration gui = ConfigHandler.getInstance().getGui();

    public static GuiItem getPlayerSearchInfo(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.player-search-info"),
            LanguageHandler.getText(language, "items.player-search-info.name"),
            LanguageHandler.getTextList(language, "items.player-search-info.lore"), false
        );
    }

    public static GuiItem getBackItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.back"),
            LanguageHandler.getText(language, "items.back.name"),
            LanguageHandler.getTextList(language, "items.back.lore"), false
        );
    }

    public static GuiItem getCloseItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.close"),
            LanguageHandler.getText(language, "items.close.name"),
            LanguageHandler.getTextList(language, "items.close.lore"), false
        );
    }

    public static GuiItem getPreviousPageItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.previous-page"),
            LanguageHandler.getText(language, "items.previous-page.name"),
            LanguageHandler.getTextList(language, "items.previous-page.lore"), false
        );
    }
    
    public static GuiItem getNextPageItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.next-page"),
            LanguageHandler.getText(language, "items.next-page.name"),
            LanguageHandler.getTextList(language, "items.next-page.lore"), false
        );
    }

    public static GuiItem getFillerItem() {
        return buildGuiItem(
            gui.getString("item-materials.filler"),
            " ",
            List.of(), false
        );
    }

    public static GuiItem getRejectItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.reject"),
            LanguageHandler.getText(language, "items.reject.name"),
            LanguageHandler.getTextList(language, "items.reject.lore"), false
        );
    }

    public static GuiItem getAcceptItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.accept"),
            LanguageHandler.getText(language, "items.accept.name"),
            LanguageHandler.getTextList(language,   "items.accept.lore"), false
        );
    }

    public static GuiItem getConfirmItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.confirm"),
            LanguageHandler.getText(language, "items.confirm.name"),
            LanguageHandler.getTextList(language, "items.confirm.lore"), false
        );
    }

    public static GuiItem getCancelItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.cancel"),
            LanguageHandler.getText(language, "items.cancel.name"),
            LanguageHandler.getTextList(language, "items.cancel.lore"), false
        );
    }

    public static GuiItem getPromoteItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.promote"),
            LanguageHandler.getText(language, "items.promote.name"),
            LanguageHandler.getTextList(language, "items.promote.lore"), false
        );
    }

    public static GuiItem getSearchItem(String searchTerm, String search, Language language) {
        List<String> lore = new ArrayList<String>();
        if (search != null && !search.isBlank()) {
            lore.add(LanguageHandler.getText(language, "items.search.searched").replace("%search%", search));
            lore.add(LanguageHandler.getText(language, "items.search.search-again-Line-1"));
            lore.add(LanguageHandler.getText(language, "items.search.search-again-Line-2"));
        } else {
            lore.add(LanguageHandler.getText(language, "items.search.search-line"));
        }
        return buildGuiItem(
            gui.getString("item-materials.search"),
            LanguageHandler.getText(language,"items.search.name").replace("%searchTerm%", searchTerm),
            lore, (search != null && !search.isBlank())
        );
    }

    public static GuiItem getNotePadItem(Boolean isSelected, Language language  ) {
        List<String> lore = LanguageHandler.getTextList(language, "items.notepad.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.selected"));
        
        return buildGuiItem(
            gui.getString("item-materials.notepad"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.notepad.name"),
            lore, isSelected
        );
    }

    public static GuiItem getGeneralConfigItem(Language language, String name, Boolean value) { // name -> Nombre de la sección de la config en gui.yml. Ej: "global-chat-on-join"
        return getConfigItem(language, "general", name, value);
    }

    public static GuiItem getManagerConfigItem(Language language, String name, Boolean value) { // name -> Nombre de la sección de la config en gui.yml. Ej: "notifications"
        return getConfigItem(language, "manager", name, value);
    }

    public static GuiItem getReviewerConfigItem(Language language, String name, Boolean value) { // name -> Nombre de la sección de la config en gui.yml. Ej: "notifications"
        return getConfigItem(language, "reviewer", name, value);
    }

    public static GuiItem getLanguageSelectionItem(Language language, Boolean isSelected) {
        String langCode = language.getCode();
        String path = "item-materials.configs.general.lang.langs." + langCode;
        String headId = gui.getString(path);
        
        List<String> lore = new ArrayList<>();
        if (isSelected) lore.add(LanguageHandler.getText(language, "placeholder.selected"));
        
        return buildGuiItem(
            headId,
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "placeholder.lang-mc." + langCode),
            lore,
            false
        );
    }

    public static GuiItem getLangConfigItem(Language language) {
        String displayName = LanguageHandler.getText(language, "items.config.name");
        displayName = displayName.replace("%name%", LanguageHandler.getText(language, "items.config.configs.general.lang.name"));
        
        List<String> lore = LanguageHandler.getTextList(language, "items.config.configs.general.lang.desc");
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("%current%", LanguageHandler.getText(language, "placeholder.lang-mc." + language.getCode()));
            processedLore.add(line);
        }
        return buildGuiItem(gui.getString("item-materials.configs.general.lang.item"), displayName, processedLore, false);
    }

    private static GuiItem getConfigItem(Language language, String context, String name, Boolean value) {
        String displayName = LanguageHandler.getText(language, "items.config.name");
        displayName = displayName.replace("%name%", LanguageHandler.getText(language, "items.config.configs." + context + "." + name + ".name"));

        List<String> displayDesc = LanguageHandler.getTextList(language, "items.config.configs." + context + "." + name + ".desc");
        List<String> processedDesc = new ArrayList<>();
        for (String line : displayDesc) {
            String descLine = LanguageHandler.getText(language, "items.config.desc-line");
            descLine = descLine.replace("%desc%", line);
            processedDesc.add(descLine);
        }

        String valueLine = LanguageHandler.getText(language, "items.config.value-line");
        valueLine = valueLine.replace("%value%", value ? LanguageHandler.getText(language, "items.config.value-true") : LanguageHandler.getText(language, "items.config.value-false"));
        processedDesc.add(valueLine);

        String trueMaterial = gui.getString("item-materials.configs." + context + "." + name + ".true");
        String falseMaterial = gui.getString("item-materials.configs." + context + "." + name + ".false");
        return buildGuiItem(value ? trueMaterial : falseMaterial, displayName, processedDesc, value);
    }

    private static GuiItem buildGuiItem(String materialName, String name, List<String> lore, Boolean isEnchanted) {
        if (materialName != null && materialName.startsWith("hdb:")) {
            String headId = materialName.substring(4);
            ItemStack headItem = HeadDBUtil.get(headId);
            return buildGuiItem(headItem, name, lore, isEnchanted);
        }
        ItemBuilder builder = ItemBuilder.from(getMaterialFromString(materialName))
            .name(MiniMessage.miniMessage().deserialize("<!italic>" + name))
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        if (isEnchanted) builder.enchant(Enchantment.CHANNELING);
            
        List<Component> components = new ArrayList<>();
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                components.add(MiniMessage.miniMessage().deserialize("<!italic>" + line));
            }
            builder.lore(components);
        }
        return builder.asGuiItem();
    }

    private static GuiItem buildGuiItem(ItemStack itemStack, String name, List<String> lore, Boolean isEnchanted) {
        ItemBuilder builder = ItemBuilder.from(itemStack)
            .name(MiniMessage.miniMessage().deserialize("<!italic>" + name))
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        
        List<Component> components = new ArrayList<>();
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                components.add(MiniMessage.miniMessage().deserialize("<!italic>" + line));
            }
            builder.lore(components);
        }

        if (isEnchanted) builder.enchant(Enchantment.CHANNELING);
        
        return builder.asGuiItem();
    }

    private static Material getMaterialFromString(String materialName) {
        if (materialName == null || materialName.isBlank()) {
            ConsoleLogger.warn("El material no puede ser nulo o vacío, usando STONE como predeterminado.");
            return Material.STONE;
        }
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            ConsoleLogger.warn("Material no encontrado: " + materialName + ", usando STONE como predeterminado.");
            return Material.STONE;
        }
    }

    public static GuiItem getArgentinaHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.argentina-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.argentina-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.argentina-head.name"),
            lore, false
        );
    }

    public static GuiItem getChileHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.chile-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.chile-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.chile-head.name"),
            lore, false
        );
    }

    public static GuiItem getUruguayHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.uruguay-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.uruguay-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.uruguay-head.name"),
            lore, false
        );
    }

    public static GuiItem getParaguayHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.paraguay-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.paraguay-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.paraguay-head.name"),
            lore, false
        );
    }

    public static GuiItem getBoliviaHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.bolivia-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.bolivia-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.bolivia-head.name"),
            lore, false
        );
    }

    public static GuiItem getPeruHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language,   "items.peru-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));  
        return buildGuiItem(
            gui.getString("item-materials.peru-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.peru-head.name"),
            lore, false
        );
    }

    public static GuiItem getGlobalChatHeadItem(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.global-chat-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.global-chat-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.global-chat-head.name"),
            lore, false
        );
    }

    public static GuiItem getInternationalHead(Boolean isSelected, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.international-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString("item-materials.international-head"),
            (isSelected ? "<b>" : "") + LanguageHandler.getText(language, "items.international-head.name"),
            lore,false
        );
    }

    public static GuiItem getRangoUsuario(RangoUsuario rangoUsuario, Boolean isSelected, Language language) {
        List<String> lore = new ArrayList<>();
        String path = "items.rango-usuario." + rangoUsuario.getNombre().toLowerCase();
        String materialPath = "item-materials.rango-usuario." + rangoUsuario.getNombre().toLowerCase();
        lore.add(LanguageHandler.replaceMC("items.rango-usuario.description", language, rangoUsuario));
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString(materialPath),
            (isSelected ? "<b>" : "") + LanguageHandler.replaceMC(path + ".name", language, rangoUsuario),
            lore, false
        );
    }

    public static GuiItem getTipoUsuario(TipoUsuario tipoUsuario, Boolean isSelected, Language language) {
        List<String> lore = new ArrayList<>();
        String path = "items.tipo-usuario." + tipoUsuario.getNombre().toLowerCase();
        String materialPath = "item-materials.tipo-usuario." + tipoUsuario.getNombre().toLowerCase();
        lore.add(LanguageHandler.replaceMC("items.tipo-usuario.description", language, tipoUsuario));
        lore.add(LanguageHandler.replaceMC("items.tipo-usuario.max-projects", language, tipoUsuario));
        if (isSelected) lore.addFirst(LanguageHandler.getText(language, "placeholder.item-mc.selected"));
        return buildGuiItem(
            gui.getString(materialPath),
            (isSelected ? "<b>" : "") + LanguageHandler.replaceMC(path + ".name", language, tipoUsuario),
            lore, false
        );
    }

    public static GuiItem getProyecto(Proyecto proyecto, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.proyecto.lore");
        
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            line = PlaceholderUtils.replaceMC(line, language, proyecto);
            processedLore.add(line);
        }
                
        return buildGuiItem(
            gui.getString("item-materials.proyecto-default"),
            LanguageHandler.replaceMC("items.proyecto.name", language, proyecto),
            processedLore, false
        );
    }

    public static GuiItem getMembersItem(Proyecto proyecto, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.members.lore");
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            line = PlaceholderUtils.replaceMC(line, language, proyecto);
            processedLore.add(line);
        }   
        return buildGuiItem(
            gui.getString("item-materials.members"),
            LanguageHandler.replaceMC("items.members.name", language, proyecto),
            processedLore, false
        );
    }

    public static GuiItem getMemberAddItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.add-member"),
            LanguageHandler.getText(language, "items.add-member.name"),
            LanguageHandler.getTextList(language, "items.add-member.lore"), false
        );
    }

    public static GuiItem getMemberRemoveItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.remove-member"),
            LanguageHandler.getText(language, "items.remove-member.name"),
            LanguageHandler.getTextList(language, "items.remove-member.lore"), false
        );
    }

    public static GuiItem getLiderTransferItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.transfer-leader"),
            LanguageHandler.getText(language, "items.transfer-leader.name"),
            LanguageHandler.getTextList(language, "items.transfer-leader.lore"), false
        );
    }

    public static GuiItem getSetNameDescription(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.set-name-description"),
            LanguageHandler.getText(language, "items.set-name-description.name"),
            LanguageHandler.getTextList(language, "items.set-name-description.lore"), false
        );
    }

    public static GuiItem getFinishProjectItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.finish-project"),
            LanguageHandler.getText(language, "items.finish-project.name"),
            LanguageHandler.getTextList(language, "items.finish-project.lore"), false
        );
    }

    public static GuiItem getLeaveProjectItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.leave-project"),
            LanguageHandler.getText(language, "items.leave-project.name"),
            LanguageHandler.getTextList(language, "items.leave-project.lore"), false
        );
    }

    public static GuiItem getNotificationsItem(int cantNotificaciones, Language language) {
        List<String> lore = LanguageHandler.getTextList(language, "items.notifications.lore");
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("%notificaciones%", String.valueOf(cantNotificaciones));
            processedLore.add(line);
        }
        return buildGuiItem(
            gui.getString("item-materials.notifications"),
            LanguageHandler.getText(language, "items.notifications.name"),
            processedLore, cantNotificaciones > 0
        );
    }

    public static GuiItem getClaimProjectItem(Language language) {
        return buildGuiItem(
            gui.getString("item-materials.claim"),
            LanguageHandler.getText(language, "items.claim.name"),
            LanguageHandler.getTextList(language, "items.claim.lore"), false
        );
    }

    public static GuiItem getPlayerItem(Player player, Boolean isOnline, PlayerContext context, Language language) {
        String name = LanguageHandler.getText(language, "items.player.name");
        if (player == null) name = name.replace("%player.nombre%", LanguageHandler.getText(language, "items.player.no-player-name"));
        else name = PlaceholderUtils.replaceMC(name, language, player);
        String contexto = "";
        if (context != null) {
            switch (context) {
                case LIDER:
                    contexto = LanguageHandler.getText(language, "items.player.contexto.lider");
                    break;
                case MIEMBRO:
                    contexto = LanguageHandler.getText(language, "items.player.contexto.miembro");
                    break;
                case DEFAULT:
                    contexto = LanguageHandler.getText(language, "items.player.contexto.default");
                    break;
            }
        } else {
            name = name.replace("%contexto% ", "");
        }
        name = name.replace("%contexto%", contexto);
        if (player == null) {
            List<String> lore = new ArrayList<>();
            for (String line : LanguageHandler.getTextList(language, "items.player.no-player-lore")) {
                line = line.replace("%contexto%", contexto);
                lore.add(line);
            }
            return buildGuiItem(
                gui.getString("item-materials.player-head"),
                name,
                lore, false
            );
        }
        List<String> lore = LanguageHandler.getTextList(language, "items.player.lore");
        
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            line = PlaceholderUtils.replaceMC(line, language, player);
            processedLore.add(line);
        }
        return buildGuiItem(
            HeadDBUtil.getPlayerHead(player.getUuid(), context == PlayerContext.LIDER || context == PlayerContext.MIEMBRO || isOnline),
            name,
            processedLore,
            false
        );
    }

    public static enum PlayerContext {
        LIDER, MIEMBRO, DEFAULT
    }

    public static boolean createSignGUI(org.bukkit.entity.Player player, SignGUIFinishHandler handler, Language language) {
        SignGUI gui;
        try {
            List<String> signLines = LanguageHandler.getTextList(language, "sign-gui");
            gui = SignGUI.builder()
                .setLines(signLines.toArray(new String[0]))
                .setHandler(handler)
                .build();
            gui.open(player);
            return true;
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("sign-gui-create-error").replace("%reason%", e.getMessage()));
            e.printStackTrace();
        }
        return false;
    }

}
