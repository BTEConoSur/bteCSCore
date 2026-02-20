package com.bteconosur.db.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DateUtils;
import com.bteconosur.core.util.TerraUtils;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class PlaceholderUtils {


    public static String replaceMC(String text, Language language, List<Player> players, List<Proyecto> proyectos) {
        String replaced = replace(text, language, PlaceholderContext.MINECRAFT, players.toArray(new Player[0]));
        return replace(replaced, language, PlaceholderContext.MINECRAFT, proyectos.toArray(new Proyecto[0]));
    }

    public static String replaceDS(String text, Language language, List<Player> players, List<Proyecto> proyectos) {
        String replaced = replace(text, language, PlaceholderContext.DISCORD, players.toArray(new Player[0]));
        return replace(replaced, language, PlaceholderContext.DISCORD, proyectos.toArray(new Proyecto[0]));
    }

    public static String replaceMC(String text, Language language, Player... players) {
        return replace(text, language, PlaceholderContext.MINECRAFT, players);
    }

    public static String replaceDS(String text, Language language, Player... players) {
        return replace(text, language, PlaceholderContext.DISCORD, players);
    }

    public static String replaceMC(String text, Language language, Proyecto... proyectos) {
        return replace(text, language, PlaceholderContext.MINECRAFT, proyectos);
    }

    public static String replaceDS(String text, Language language, Proyecto... proyectos) {
        return replace(text, language, PlaceholderContext.DISCORD, proyectos);
    }

    public static String replaceMC(String text, Language language, Pais... paises) {
        return replace(text, language, PlaceholderContext.MINECRAFT, paises);
    }

    public static String replaceDS(String text, Language language, Pais... paises) {
        return replace(text, language, PlaceholderContext.DISCORD, paises);
    }

    public static String replaceMC(String text, Language language, RangoUsuario... rangos) {
        return replace(text, language, PlaceholderContext.MINECRAFT, rangos);
    }

    public static String replaceDS(String text, Language language, RangoUsuario... rangos) {
        return replace(text, language, PlaceholderContext.DISCORD, rangos);
    }

    public static String replaceMC(String text, Language language, TipoUsuario... tipos) {
        return replace(text, language, PlaceholderContext.MINECRAFT, tipos);
    }

    public static String replaceDS(String text, Language language, TipoUsuario... tipos) {
        return replace(text, language, PlaceholderContext.DISCORD, tipos);
    }

    public static String replaceMC(String text, Language language, Division... divisiones) {
        return replace(text, language, PlaceholderContext.MINECRAFT, divisiones);
    }

    public static String replaceDS(String text, Language language, Division... divisiones) {
        return replace(text, language, PlaceholderContext.DISCORD, divisiones);
    }

    private static String replace(String text, Language language, PlaceholderContext context, Player... players) {
        if (text == null) return "TEXTO_NULL";
        List<String> playerTokens = new ArrayList<>();
        int i = 0;
        while (true) {
            int start = text.indexOf("%player.", i);
            if (start == -1) break;
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;
            String withoutPrefix = text.substring(start + 1, end).substring("player.".length()); // "nombre.2"
            playerTokens.add(withoutPrefix);
            i = end + 1;
        }

        for (String token : playerTokens) {
            String[] parts = token.split("\\.");
            String field = parts[0];
            int index = 0;
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    ConsoleLogger.warn("Índice no válido en placeholder: " + token);
                    continue;
                }
            }

            Player p = (index >= 0 && index < players.length) ? players[index] : null;
            String value = "";
            String path;
            if (p != null) {
                switch (field) {
                    case "nombre":
                        value = p.getNombre() != null ? p.getNombre() : "ERROR_NOMBRE_NULL";
                        break;
                    case "uuid":
                        value = p.getUuid() != null ? p.getUuid().toString() : "ERROR_UUID_NULL";
                        break;
                    case "nombrePublico":
                        value = p.getNombrePublico() != null ? p.getNombrePublico() : "ERROR_NOMBRE_PUBLICO_NULL";
                        break;
                    case "fechaIngreso":
                        value = DateUtils.formatDate(p.getFechaIngreso(), language);
                        break;
                    case "fechaUltimaConexion":
                        value = DateUtils.formatDate(p.getFechaUltimaConexion(), language);
                        break;
                    case "tipoUsuario":
                        value = p.getTipoUsuario() != null ? p.getTipoUsuario().getNombre() : "ERROR_TIPO_USUARIO_NULL";
                        break;
                    case "rangoUsuario":
                        value = p.getRangoUsuario() != null ? p.getRangoUsuario().getNombre() : "RANGO_USUARIO_NULL";
                        break;
                    case "rangoUsuarioPrefijo":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.rango-mc.";
                        else path = "placeholder.rango-ds.";
                        value = p.getRangoUsuario() != null ? LanguageHandler.getText(language, path + p.getRangoUsuario().getNombre().toLowerCase()) : "ERROR_RANGO_USUARIO_NULL";
                        break;
                    case "tipoUsuarioPrefijo":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.tipo-mc.";
                        else path = "placeholder.tipo-ds.";
                        value = p.getTipoUsuario() != null ? LanguageHandler.getText(language, path + p.getTipoUsuario().getNombre().toLowerCase()) : "ERROR_TIPO_USUARIO_NULL";
                        break; 
                    case "paisPrefix":
                        Pais pais = p.getPaisPrefix();
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.pais-mc.prefix.";
                        else path = "placeholder.pais-ds.prefix.";
                        if (pais != null) value = LanguageHandler.getText(language, path + pais.getNombre().toLowerCase());
                        else value = LanguageHandler.getText(language, path + "internacional");
                        break;
                    
                    case "estado":
                        PlayerRegistry registry = PlayerRegistry.getInstance();
                        boolean isOnline = registry.isOnline(p.getUuid());
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.player-mc.";
                        else path = "placeholder.player-ds.";
                        value = LanguageHandler.getText(language, path + (isOnline ? "online" : "offline"));
                        break;
                    case "proyectosActivos":
                        int count = ProyectoRegistry.getInstance().getActivosCount(p);
                        value = String.valueOf(count);
                        break;
                    case "proyectosCompletados":
                        int countFinalizados = ProyectoRegistry.getInstance().getCompletadosCount(p);
                        value = String.valueOf(countFinalizados);
                        break;
                    default:
                        value = "";
                        break;
                }
            }

            text = text.replace("%player." + token + "%", value);
        }

        return text;
    }   

    private static String replace(String text, Language language, PlaceholderContext context, Proyecto... proyectos) {
        if (text == null) return "TEXTO_NULL";
        List<String> proyectoTokens = new ArrayList<>();
        int i = 0;
        while (true) {
            int start = text.indexOf("%proyecto.", i);
            if (start == -1) break;
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;
            String withoutPrefix = text.substring(start + 1, end).substring("proyecto.".length());
            proyectoTokens.add(withoutPrefix);
            i = end + 1;
        }

        for (String token : proyectoTokens) {
            String[] parts = token.split("\\.");
            String field = parts[0];
            int index = 0;
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    ConsoleLogger.warn("Índice no válido en placeholder: " + token);
                    continue;
                }
            }

            Proyecto proyecto = (index >= 0 && index < proyectos.length) ? proyectos[index] : null;
            String value = "";
            String path;
            if (proyecto != null) {
                switch (field) {
                    case "id":
                        value = proyecto.getId() != null ? proyecto.getId() : "ERROR_ID_NULL";
                        break;
                    case "nombre":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.proyecto-mc.";
                        else path = "placeholder.proyecto-ds.";
                        value = proyecto.getNombre() != null && !proyecto.getNombre().isBlank() ? proyecto.getNombre() : LanguageHandler.getText(language, path + "sin-nombre"); 
                        break;
                    case "descripcion":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.proyecto-mc.";
                        else path = "placeholder.proyecto-ds.";
                        value = proyecto.getDescripcion() != null && !proyecto.getDescripcion().isBlank() ? proyecto.getDescripcion() : LanguageHandler.getText(language, path + "sin-descripcion");
                        break;
                    case "estado":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.proyecto-mc.estado.";
                        else path = "placeholder.proyecto-ds.estado.";
                        switch (proyecto.getEstado()) {
                            case ACTIVO:
                                value = LanguageHandler.getText(language, path + "activo");
                                break;
                            case EN_FINALIZACION:
                                value = LanguageHandler.getText(language, path + "en-finalizacion");
                                break;
                            case EN_FINALIZACION_EDICION:
                                value = LanguageHandler.getText(language, path + "en-finalizacion-edit");
                                break;
                            case COMPLETADO:
                                value = LanguageHandler.getText(language, path + "completado");
                                break;
                            case EN_CREACION:
                                value = LanguageHandler.getText(language, path + "en-creacion");
                                break;
                            case REDEFINIENDO:
                                value = LanguageHandler.getText(language, path + "redefiniendo");
                                break;
                            case ABANDONADO:
                                value = LanguageHandler.getText(language, path + "abandonado");
                                break;
                            case EDITANDO:
                                value = LanguageHandler.getText(language, path + "editando");
                                break;
                            default:
                                value = "ERROR_MAL_ESTADO";
                                break;
                        }
                        break;
                    case "tamano":
                        value = String.valueOf(proyecto.getTamaño());
                        break;
                    case "paisId":
                        value = proyecto.getPais() != null ? String.valueOf(proyecto.getPais().getId()) : "ERROR_PAIS_NULL";
                        break;
                    case "paisNombre":
                        value = proyecto.getPais() != null ? proyecto.getPais().getNombrePublico() : "ERROR_PAIS_NULL";
                        break;
                    case "tipoId":
                        value = proyecto.getTipoProyecto() != null ? String.valueOf(proyecto.getTipoProyecto().getId()) : "ERROR_TIPO_NULL";
                        break;
                    case "tipo":
                        value = proyecto.getTipoProyecto() != null ? proyecto.getTipoProyecto().getNombre() : "ERROR_TIPO_NULL";
                        break;
                    case "lider":
                        Player lider = ProjectManager.getInstance().getLider(proyecto);
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.proyecto-mc.";
                        else path = "placeholder.proyecto-ds.";
                        value = lider != null ? lider.getNombrePublico() : LanguageHandler.getText(language, path + "sin-lider");
                        break;
                    case "miembrosCantidad":
                        int miembrosCount = proyecto.getCantMiembros();
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.proyecto-mc.";
                        else path = "placeholder.proyecto-ds.";
                        value = miembrosCount > 0 ? String.valueOf(miembrosCount) : LanguageHandler.getText(language, path + "sin-miembros");
                        break;
                    case "fechaCreacion":
                        value = DateUtils.formatDate(proyecto.getFechaCreado(), language);
                        break;
                    case "fechaFinalizacion":
                        value = DateUtils.formatDate(proyecto.getFechaTerminado(), language);
                        break;
                    case "tipoProyecto":
                        value = proyecto.getTipoProyecto() != null ? proyecto.getTipoProyecto().getNombre() : "ERROR_TIPO_PROYECTO_NULL";
                        break;
                    case "maxMiembros":
                        value = proyecto.getTipoProyecto() != null ? String.valueOf(proyecto.getTipoProyecto().getMaxMiembros()) : "ERROR_TIPO_PROYECTO_NULL";
                        break;
                    case "divisionId":
                        value = proyecto.getDivision() != null ? String.valueOf(proyecto.getDivision().getId()) : "ERROR_DIVISION_NULL";
                        break;
                    case "divisionNombre":
                        value = proyecto.getDivision() != null ? proyecto.getDivision().getNombre() : "ERROR_DIVISION_NULL";
                        break;
                    case "divisionGna":
                        value = proyecto.getDivision() != null ? proyecto.getDivision().getGna() : "ERROR_DIVISION_NULL";
                        break;
                    case "divisionFna":
                        value = proyecto.getDivision() != null ? proyecto.getDivision().getFna() : "ERROR_DIVISION_NULL";
                        break;
                    case "divisionNam":
                        value = proyecto.getDivision() != null ? proyecto.getDivision().getNam() : "ERROR_DIVISION_NULL";
                        break;
                    case "divisionContexto":
                        value = proyecto.getDivision() != null ? proyecto.getDivision().getContexto() : "ERROR_DIVISION_NULL";
                        break;
                    case "geoCoords":
                        Point centroid = proyecto.getPoligono().getCentroid();
                        double[] geoCoords = TerraUtils.toGeo(centroid.getX(), centroid.getY());
                        value = geoCoords[1] + ", " + geoCoords[0];
                        break;
                    default:
                        value = "ERROR_MAL_CAMPO";
                        break;
                }
            }

            text = text.replace("%proyecto." + token + "%", value);
        }

        return text;
    }

    private static String replace(String text, Language language, PlaceholderContext context, Pais... paises) {
        if (text == null) return "TEXTO_NULL";
        List<String> paisTokens = new ArrayList<>();
        int i = 0;
        while (true) {
            int start = text.indexOf("%pais.", i);
            if (start == -1) break;
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;
            String withoutPrefix = text.substring(start + 1, end).substring("pais.".length());
            paisTokens.add(withoutPrefix);
            i = end + 1;
        }

        for (String token : paisTokens) {
            String[] parts = token.split("\\.");
            String field = parts[0];
            int index = 0;
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    ConsoleLogger.warn("Índice no válido en placeholder: " + token);
                    continue;
                }
            }

            Pais pais = (index >= 0 && index < paises.length) ? paises[index] : null;
            String value = "";
            if (pais != null) {
                switch (field) {
                    case "id":
                        value = pais.getId() != null ? pais.getId().toString() : "ERROR_ID_NULL";
                        break;
                    case "nombre":
                        value = pais.getNombre() != null ? pais.getNombre() : "ERROR_NOMBRE_NULL";
                        break;
                    case "nombrePublico":
                        value = pais.getNombrePublico() != null ? pais.getNombrePublico() : "ERROR_NOMBRE_PUBLICO_NULL";
                        break;
                    case "dsIdGuild":
                        value = pais.getDsIdGuild() != null ? pais.getDsIdGuild().toString() : "ERROR_DS_ID_GUILD_NULL";
                        break;
                    case "dsIdGlobalChat":
                        value = pais.getDsIdGlobalChat() != null ? pais.getDsIdGlobalChat().toString() : "ERROR_DS_ID_GLOBAL_CHAT_NULL";
                        break;
                    case "dsIdCountryChat":
                        value = pais.getDsIdCountryChat() != null ? pais.getDsIdCountryChat().toString() : "ERROR_DS_ID_COUNTRY_CHAT_NULL";
                        break;
                    case "dsIdLog":
                        value = pais.getDsIdLog() != null ? pais.getDsIdLog().toString() : "ERROR_DS_ID_LOG_NULL";
                        break;
                    case "dsIdRequest":
                        value = pais.getDsIdRequest() != null ? pais.getDsIdRequest().toString() : "ERROR_DS_ID_REQUEST_NULL";
                        break;
                    case "logo":
                        if (context == PlaceholderContext.MINECRAFT) value = "ERROR_LOGO_MC";
                        else value = LanguageHandler.getText(language, "placeholder.pais-ds.logo." + pais.getNombre().toLowerCase());
                        break;
                    default:
                        value = "";
                        break;
                }
            }

            text = text.replace("%pais." + token + "%", value);
        }

        return text;
    }

    private static String replace(String text, Language language, PlaceholderContext context, RangoUsuario... rangos) {
        if (text == null) return "TEXTO_NULL";
        List<String> rangoTokens = new ArrayList<>();
        int i = 0;
        while (true) {
            int start = text.indexOf("%rango.", i);
            if (start == -1) break;
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;
            String withoutPrefix = text.substring(start + 1, end).substring("rango.".length());
            rangoTokens.add(withoutPrefix);
            i = end + 1;
        }

        for (String token : rangoTokens) {
            String[] parts = token.split("\\.");
            String field = parts[0];
            int index = 0;
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    ConsoleLogger.warn("Índice no válido en placeholder: " + token);
                    continue;
                }
            }

            RangoUsuario rango = (index >= 0 && index < rangos.length) ? rangos[index] : null;
            String value = "";
            String path;
            if (rango != null) {
                switch (field) {
                    case "id":
                        value = rango.getId() != null ? rango.getId().toString() : "ERROR_ID_NULL";
                        break;
                    case "nombre":
                        value = rango.getNombre() != null ? rango.getNombre() : "ERROR_NOMBRE_NULL";
                        break;
                    case "descripcion":
                        value = rango.getDescripcion() != null ? rango.getDescripcion() : "ERROR_DESCRIPCION_NULL";
                        break;
                    case "prefijo":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.rango-mc.";
                        else path = "placeholder.rango-ds.";
                        value = rango.getNombre() != null ? LanguageHandler.getText(language, path + rango.getNombre().toLowerCase()) : "ERROR_NOMBRE_NULL";
                        break;
                    case "playersCantidad":
                        int playersCount = rango.getPlayers() != null ? rango.getPlayers().size() : 0;
                        value = String.valueOf(playersCount);
                        break;
                    case "permisosCantidad":
                        int permisosCount = rango.getPermisos() != null ? rango.getPermisos().size() : 0;
                        value = String.valueOf(permisosCount);
                        break;
                    default:
                        value = "";
                        break;
                }
            }

            text = text.replace("%rango." + token + "%", value);
        }

        return text;
    }

    private static String replace(String text, Language language, PlaceholderContext context, TipoUsuario... tipos) {
        if (text == null) return "TEXTO_NULL";
        List<String> tipoTokens = new ArrayList<>();
        int i = 0;
        while (true) {
            int start = text.indexOf("%tipo.", i);
            if (start == -1) break;
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;
            String withoutPrefix = text.substring(start + 1, end).substring("tipo.".length());
            tipoTokens.add(withoutPrefix);
            i = end + 1;
        }

        for (String token : tipoTokens) {
            String[] parts = token.split("\\.");
            String field = parts[0];
            int index = 0;
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    ConsoleLogger.warn("Índice no válido en placeholder: " + token);
                    continue;
                }
            }

            TipoUsuario tipo = (index >= 0 && index < tipos.length) ? tipos[index] : null;
            String value = "";
            String path;
            if (tipo != null) {
                switch (field) {
                    case "id":
                        value = tipo.getId() != null ? tipo.getId().toString() : "ERROR_ID_NULL";
                        break;
                    case "nombre":
                        value = tipo.getNombre() != null ? tipo.getNombre() : "ERROR_NOMBRE_NULL";
                        break;
                    case "descripcion":
                        value = tipo.getDescripcion() != null ? tipo.getDescripcion() : "ERROR_DESCRIPCION_NULL";
                        break;
                    case "prefijo":
                        if (context == PlaceholderContext.MINECRAFT) path = "placeholder.tipo-mc.";
                        else path = "placeholder.tipo-ds.";
                        value = tipo.getNombre() != null ? LanguageHandler.getText(language, path + tipo.getNombre().toLowerCase()) : "ERROR_NOMBRE_NULL";
                        break;
                    case "cantProyecSim":
                        value = tipo.getCantProyecSim() != null ? tipo.getCantProyecSim().toString() : "ERROR_CANT_PROYEC_SIM_NULL";
                        break;
                    case "permisosCantidad":
                        int permisosCount = tipo.getPermisos() != null ? tipo.getPermisos().size() : 0;
                        value = String.valueOf(permisosCount);
                        break;
                    default:
                        value = "";
                        break;
                }
            }

            text = text.replace("%tipo." + token + "%", value);
        }

        return text;
    }

    private static String replace(String text, Language language, PlaceholderContext context, Division... divisiones) {
        if (text == null) return "TEXTO_NULL";
        List<String> divisionTokens = new ArrayList<>();
        int i = 0;
        while (true) {
            int start = text.indexOf("%division.", i);
            if (start == -1) break;
            int end = text.indexOf('%', start + 1);
            if (end == -1) break;
            String withoutPrefix = text.substring(start + 1, end).substring("division.".length());
            divisionTokens.add(withoutPrefix);
            i = end + 1;
        }

        for (String token : divisionTokens) {
            String[] parts = token.split("\\.");
            String field = parts[0];
            int index = 0;
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    ConsoleLogger.warn("Índice no válido en placeholder: " + token);
                    continue;
                }
            }

            Division division = (index >= 0 && index < divisiones.length) ? divisiones[index] : null;
            String value = "";
            if (division != null) {
                switch (field) {
                    case "id":
                        value = division.getId() != null ? division.getId().toString() : "ERROR_ID_NULL";
                        break;
                    case "nombre":
                        value = division.getNombre() != null ? division.getNombre() : "ERROR_NOMBRE_NULL";
                        break;
                    case "nam":
                        value = division.getNam() != null ? division.getNam() : "ERROR_NAM_NULL";
                        break;
                    case "gna":
                        value = division.getGna() != null ? division.getGna() : "ERROR_GNA_NULL";
                        break;
                    case "fna":
                        value = division.getFna() != null ? division.getFna() : "ERROR_FNA_NULL";
                        break;
                    case "contexto":
                        value = division.getContexto() != null ? division.getContexto() : "ERROR_CONTEXTO_NULL";
                        break;
                    case "paisId":
                        value = division.getPais() != null ? division.getPais().getId().toString() : "ERROR_PAIS_NULL";
                        break;
                    case "paisNombre":
                        value = division.getPais() != null ? division.getPais().getNombrePublico() : "ERROR_PAIS_NULL";
                        break;
                    default:
                        value = "";
                        break;
                }
            }

            text = text.replace("%division." + token + "%", value);
        }

        return text;
    }

    public static enum PlaceholderContext {
        DISCORD,
        MINECRAFT
    }   
}
