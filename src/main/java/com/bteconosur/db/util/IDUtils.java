package com.bteconosur.db.util;

import java.util.Random;

import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.discord.util.LinkService;
import com.bteconosur.discord.util.MessageService;

/**
 * Utilidades para generación de identificadores aleatorios únicos.
 */
public class IDUtils {

    private static final DBManager dbManager = DBManager.getInstance();

    /**
     * Genera un código único de 6 letras mayúsculas para proyectos.
     *
     * @return código de proyecto no utilizado.
     */
    public static String generarCodigoProyecto() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);
        
        do {
            codigo = new StringBuilder(6);
            for (int i = 0; i < 6; i++) codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        } while (dbManager.exists(Proyecto.class, codigo.toString()));

        return codigo.toString();
    }

    /**
     * Genera un código único de 6 caracteres para vinculación Minecraft/Discord.
     *
     * @return código de enlace no utilizado.
     */
    public static String generarCodigoLink() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);
        
        do {
            codigo = new StringBuilder(6);
            for (int i = 0; i < 6; i++) codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        } while (LinkService.isMinecraftCodeValid(codigo.toString()) || LinkService.isDiscordCodeValid(codigo.toString()));

        return codigo.toString();
    }

    /**
     * Genera un código único de 6 caracteres para referencias de mensajes.
     *
     * @return código de mensaje no utilizado.
     */
    public static String generarCodigoMessage() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);
        
        do {
            codigo = new StringBuilder(6);
            for (int i = 0; i < 6; i++) codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        } while (MessageService.hasMessageRefs(codigo.toString()));

        return codigo.toString();
    }
    
}
