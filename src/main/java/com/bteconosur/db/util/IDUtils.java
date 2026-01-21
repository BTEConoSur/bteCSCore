package com.bteconosur.db.util;

import java.util.Random;

import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.discord.util.LinkService;

public class IDUtils {

    private static final DBManager dbManager = DBManager.getInstance();

    public static String generarCodigoProyecto() {
        String caracteres = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);
        
        do {
            codigo = new StringBuilder(6);
            for (int i = 0; i < 6; i++) codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        } while (dbManager.exists(Proyecto.class, codigo.toString()));

        return codigo.toString();
    }

    public static String generarCodigoLink() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);
        
        do {
            codigo = new StringBuilder(6);
            for (int i = 0; i < 6; i++) codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        } while (LinkService.isMinecraftCodeValid(caracteres) || LinkService.isDiscordCodeValid(caracteres));

        return codigo.toString();
    }
    
}
