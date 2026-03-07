package com.bteconosur.core.command.btecs.test;

import java.util.List;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.TipoProyectoRegistry;

public class TestCheckProyectoInfoCommand extends BaseCommand {

    public TestCheckProyectoInfoCommand() {
        super("checkinfo", "", "btecs.command.btecs.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

        DBManager dbManager = DBManager.getInstance();
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        TipoProyectoRegistry tipoRegistry = TipoProyectoRegistry.getInstance();
        List<Proyecto> proyectos = dbManager.selectAll(Proyecto.class);

        int updatedTamaño = 0;
        int updatedDivision = 0;
        int updatedTipo = 0;
        int skipped = 0;

        PlayerLogger.info(player, "Iniciando checkinfo para " + proyectos.size() + " proyectos...", (String) null);

        for (Proyecto proyecto : proyectos) {
            if (proyecto.getPoligono() == null) {
                skipped++;
                continue;
            }

            boolean changed = false;

            proyecto.updateTamaño();
            updatedTamaño++;
            changed = true;

            if (proyecto.getDivision() == null) {
                Pais pais = paisRegistry.findByPolygon(proyecto.getPoligono());
                if (pais != null) {
                    Division division = paisRegistry.findDivisionByPolygon(proyecto.getPoligono(), pais);
                    if (division != null) {
                        proyecto.setDivision(division);
                        updatedDivision++;
                        changed = true;
                    }
                }
            }

            if (proyecto.getTipoProyecto() == null) {
                double tamaño = proyecto.getTamaño() == 0 ? proyecto.getPoligono().getArea() : proyecto.getTamaño();
                TipoProyecto tipo = tipoRegistry.get(tamaño);
                if (tipo != null) {
                    proyecto.setTipoProyecto(tipo);
                    updatedTipo++;
                    changed = true;
                }
            }

            if (changed) {
                dbManager.merge(proyecto);
            }
        }

        PlayerLogger.info(player, "Proceso completado: " + updatedTamaño + " tamaños, " + updatedDivision + " divisiones, " + updatedTipo + " tipos actualizados. " + skipped + " proyectos sin polígono omitidos.", (String) null);
        return true;
    }

}
