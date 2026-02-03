package com.bteconosur.core.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIFinishHandler;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MenuUtils {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static GuiItem getBackItem() {
        return buildGuiItem(
            lang.getString("items.back.material"),
            lang.getString("items.back.name"),
            lang.getStringList("items.back.lore"), false
        );
    }

    public static GuiItem getCloseItem() {
        return buildGuiItem(
            lang.getString("items.close.material"),
            lang.getString("items.close.name"),
            lang.getStringList("items.close.lore"), false
        );
    }

    public static GuiItem getPreviousPageItem() {
        return buildGuiItem(
            lang.getString("items.previous-page.material"),
            lang.getString("items.previous-page.name"),
            lang.getStringList("items.previous-page.lore"), false
        );
    }
    
    public static GuiItem getNextPageItem() {
        return buildGuiItem(
            lang.getString("items.next-page.material"),
            lang.getString("items.next-page.name"),
            lang.getStringList("items.next-page.lore"), false
        );
    }

    public static GuiItem getFillerItem() {
        return buildGuiItem(
            lang.getString("items.filler.material"),
            lang.getString("items.filler.name"),
            lang.getStringList("items.filler.lore"), false
        );
    }

    public static GuiItem getConfirmItem() {
        return buildGuiItem(
            lang.getString("items.confirm.material"),
            lang.getString("items.confirm.name"),
            lang.getStringList("items.confirm.lore"), false
        );
    }

    public static GuiItem getCancelItem() {
        return buildGuiItem(
            lang.getString("items.cancel.material"),
            lang.getString("items.cancel.name"),
            lang.getStringList("items.cancel.lore"), false
        );
    }

    public static GuiItem getPromoteItem() {
        return buildGuiItem(
            lang.getString("items.promote.material"),
            lang.getString("items.promote.name"),
            lang.getStringList("items.promote.lore"), false
        );
    }

    public static GuiItem getSearchItem(String searchTerm, String search) {
        List<String> lore = new ArrayList<String>();
        if (search != null && !search.isBlank()) {
            lore.add(lang.getString("items.search.searched").replace("%search%", search));
            lore.add(lang.getString("items.search.search-again-Line-1"));
            lore.add(lang.getString("items.search.search-again-Line-2"));
        } else {
            lore.add(lang.getString("items.search.search-line"));
        }
        return buildGuiItem(
            lang.getString("items.search.material"),
            lang.getString("items.search.name").replace("%searchTerm%", searchTerm),
            lore, (search != null && !search.isBlank())
        );
    }

    public static GuiItem getNotePadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.notepad.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        
        return buildGuiItem(
            lang.getString("items.notepad.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.notepad.name"),
            lore, false
        );
    }   

    public static GuiItem getGeneralConfigItem(String name, Boolean value) { // name -> Nombre de la sección de la config en lang.yml. Ej: "global-chat-on-join"
        return getConfigItem("general", name, value);
    }

    public static GuiItem getManagerConfigItem(String name, Boolean value) { // name -> Nombre de la sección de la config en lang.yml. Ej: "notifications"
        return getConfigItem("manager", name, value);
    }

    public static GuiItem getReviewerConfigItem(String name, Boolean value) { // name -> Nombre de la sección de la config en lang.yml. Ej: "notifications"
        return getConfigItem("reviewer", name, value);
    }

    private static GuiItem getConfigItem(String context, String name, Boolean value) {
        String displayName = lang.getString("items.config.name");
        displayName = displayName.replace("%name%", lang.getString("items.config.configs." + context + "." + name + ".name"));

        List<String> displayDesc = lang.getStringList("items.config.configs." + context + "." + name + ".desc");
        List<String> processedDesc = new ArrayList<>();
        for (String line : displayDesc) {
            String descLine = lang.getString("items.config.desc-line");
            descLine = descLine.replace("%desc%", line);
            processedDesc.add(descLine);
        }

        String valueLine = lang.getString("items.config.value-line");
        valueLine = valueLine.replace("%value%", value ? lang.getString("items.config.value-true") : lang.getString("items.config.value-false"));
        processedDesc.add(valueLine);

        String trueMaterial = lang.getString("items.config.configs." + context + "." + name + ".material-true");
        String falseMaterial = lang.getString("items.config.configs." + context + "." + name + ".material-false");
        return buildGuiItem(value ? trueMaterial : falseMaterial, displayName, processedDesc, false);
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
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    public static GuiItem getArgentinaHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.argentina-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.argentina-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.argentina-head.name"),
            lore, false
        );
    }

    public static GuiItem getChileHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.chile-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.chile-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.chile-head.name"),
            lore, false
        );
    }

    public static GuiItem getUruguayHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.uruguay-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.uruguay-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.uruguay-head.name"),
            lore, false
        );
    }

    public static GuiItem getParaguayHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.paraguay-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.paraguay-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.paraguay-head.name"),
            lore, false
        );
    }

    public static GuiItem getBoliviaHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.bolivia-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.bolivia-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.bolivia-head.name"),
            lore, false
        );
    }

    public static GuiItem getPeruHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.peru-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));  
        return buildGuiItem(
            lang.getString("items.peru-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.peru-head.name"),
            lore, false
        );
    }

    public static GuiItem getGlobalChatHeadItem(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.global-chat-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.global-chat-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.global-chat-head.name"),
            lore, false
        );
    }

    public static GuiItem getInternationalHead(Boolean isSelected) {
        List<String> lore = lang.getStringList("items.international-head.lore");
        if (lore == null) lore = new ArrayList<>();
        if (isSelected) lore.add(0, lang.getString("selected"));
        return buildGuiItem(
            lang.getString("items.international-head.material"),
            (isSelected ? "<b>" : "") + lang.getString("items.international-head.name"),
            lore,false
        );
    }

    public static GuiItem getRangoUsuario(RangoUsuario rangoUsuario, Boolean isSelected) {
        List<String> lore = new ArrayList<>();
        String path = "items.rango-usuario." + rangoUsuario.getNombre().toLowerCase();
        lore.add(lang.getString("items.rango-usuario.description"). replace("%descripcionRango%", rangoUsuario.getDescripcion()));
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString(path + ".material"),
            (isSelected ? "<b>" : "") + lang.getString(path + ".name").replace("%nombreRango%", rangoUsuario.getNombre()),
            lore, false
        );
    }

    public static GuiItem getTipoUsuario(TipoUsuario tipoUsuario, Boolean isSelected) {
        List<String> lore = new ArrayList<>();
        String path = "items.tipo-usuario." + tipoUsuario.getNombre().toLowerCase();
        lore.add(lang.getString("items.tipo-usuario.description"). replace("%descripcionTipo%", tipoUsuario.getDescripcion()));
        lore.add(lang.getString("items.tipo-usuario.max-projects"). replace("%maxProyectos%", String.valueOf(tipoUsuario.getCantProyecSim())));
        if (isSelected) lore.addFirst(lang.getString("selected"));
        return buildGuiItem(
            lang.getString(path + ".material"),
            (isSelected ? "<b>" : "") + lang.getString(path + ".name").replace("%nombreTipo%", tipoUsuario.getNombre()),
            lore, false
        );
    }

    public static GuiItem getProyecto(Proyecto proyecto) {
        List<String> lore = lang.getStringList("items.proyecto.lore");
        Player lider = proyecto.getLider();
        String proyectoNombre = proyecto.getNombre() != null && !proyecto.getNombre().isBlank() ? proyecto.getNombre() : "Sin Nombre";
        String descripcion = proyecto.getDescripcion() != null && !proyecto.getDescripcion().isBlank() ? proyecto.getDescripcion() : "Sin Descripción";
        String estado = null;
        switch (proyecto.getEstado()) {
            case ACTIVO:
                estado = lang.getString("items.proyecto.estado.activo");
                break;
            case EN_FINALIZACION:
                estado = lang.getString("items.proyecto.estado.en-finalizacion");
                break;
            case COMPLETADO:
                estado = lang.getString("items.proyecto.estado.completado");
                break;
            case EN_CREACION:
                estado = lang.getString("items.proyecto.estado.en-creacion");
                break;
            case REDEFINIENDO:
                estado = lang.getString("items.proyecto.estado.redefiniendo");
                break;
            case ABANDONADO:
                estado = lang.getString("items.proyecto.estado.abandonado");
                break;
            case EDITANDO:
                estado = lang.getString("items.proyecto.estado.editando");
                break;
            default:
                estado = "Reportar a administración";
                break;
        }
        
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("%descripcion%", descripcion)
                .replace("%lider%", lider != null ? lider.getNombrePublico() : "Sin Líder")
                .replace("%pais%", proyecto.getPais().getNombrePublico())
                .replace("%divisionGna%", proyecto.getDivision().getGna())
                .replace("%divisionFna%", proyecto.getDivision().getFna())
                .replace("%divisionNam%", proyecto.getDivision().getNam())
                .replace("%divisionContexto%", proyecto.getDivision().getContexto())
                .replace("%proyectoEstado%", estado)
                .replace("%tamano%", String.valueOf(proyecto.getTamaño()))
                .replace("%tipoProyecto%", proyecto.getTipoProyecto().getNombre())
                .replace("%fechaCreacion%", DateUtils.formatDate(proyecto.getFechaCreado()))
                .replace("%fechaFinalizacion%", DateUtils.formatDate(proyecto.getFechaCreado()));
            processedLore.add(line);
        }
        return buildGuiItem(
            lang.getString("items.proyecto.default-material"),
            lang.getString("items.proyecto.name").replace("%proyectoId%", proyecto.getId()).replace("%proyectoNombre%", proyectoNombre),
            processedLore, false
        );
    }

    public static boolean createSignGUI(org.bukkit.entity.Player player, SignGUIFinishHandler handler) {
        SignGUI gui;
        try {
            List<String> signLines = lang.getStringList("sign-gui");
            gui = SignGUI.builder()
                .setLines(signLines.toArray(new String[0]))
                .setHandler(handler)
                .build();
            gui.open(player);
            return true;
        } catch (Exception e) {
            ConsoleLogger.error("Error al crear el SignGUI: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
