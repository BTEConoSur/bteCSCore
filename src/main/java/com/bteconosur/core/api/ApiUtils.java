package com.bteconosur.core.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Coordinate;

import com.bteconosur.core.api.json.bteweb.ClaimRequest;
import com.bteconosur.core.api.json.bteweb.UserRef;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.TerraUtils;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.Estado;

public class ApiUtils {

	private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
	private static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    private static String getWebDescription(Proyecto proyecto) {
        if (proyecto == null) {
            return "";
        }

        Division division = proyecto.getDivision();
		String ownerName = proyecto.getLider().getNombre();
		String descripcionBase = division.getContexto() + ", " + division.getFna();
		if (proyecto.getDescripcion() != null) {
			descripcionBase += "\n\n" + proyecto.getDescripcion();
		}

		StringBuilder membersLine = new StringBuilder();
		if (proyecto.getMiembros() != null && !proyecto.getMiembros().isEmpty()) {
			for (Player miembro : proyecto.getMiembros()) {
				if (miembro == null || miembro.getNombre() == null || miembro.getNombre().isBlank()) {
					continue;
				}
				if (membersLine.length() > 0) {
					membersLine.append(", ");
				}
				membersLine.append(miembro.getNombre());
			}
		}

		String membersText = membersLine.length() > 0 ? membersLine.toString() : "Sin miembros";

		return descripcionBase + "\n\nOwner: " + ownerName + "\nMiembros: " + membersText;
    }

	public static String getToken(Pais pais) {
		if (config.getBoolean("web-debug-mode", false)) {
			return secret.getString("web-debug-token", "").trim();
		}
		if (pais == null || pais.getWebToken() == null) {
			return "";
		}
		return pais.getWebToken().trim();
	}

	public static String getBuildTeamId(Pais pais) {
		if (config.getBoolean("web-debug-mode", false)) {
			return secret.getString("web-debug-id", "").trim();
		}
		if (pais == null || pais.getWebId() == null) {
			return "";
		}
		return pais.getWebId().trim();
	}

	public static String readBody(InputStream stream) throws IOException {
		if (stream == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		return builder.toString();
	}

	public static ClaimRequest toClaimRequest(Proyecto proyecto) {
		ClaimRequest request = new ClaimRequest();

		if (proyecto == null) {
			return request;
		}

		request.setOwner(getOwnerUserRef());
		request.setBuilders(Collections.emptyList());
		request.setArea(toArrayArea(proyecto));
		request.setActive(proyecto.getEstado() != Estado.ABANDONADO);
		request.setFinished(proyecto.getEstado() == Estado.COMPLETADO);
		request.setName(proyecto.getNombre() != null ? proyecto.getNombre() : "Proyecto " + proyecto.getId());
		request.setExternalId(proyecto.getId());
		request.setDescription(getWebDescription(proyecto));
		request.setCity(proyecto.getDivision().getFna());
		request.setBuildings(0);

		return request;
	}

	private static UserRef getOwnerUserRef() {
		String ownerId = secret.getString("web-owner-id", "").trim();
        UserRef userRef = new UserRef();
        userRef.setId(UUID.fromString(ownerId));
        return userRef;
	}

	private static List<List<String>> toArrayArea(Proyecto proyecto) {
		if (proyecto == null || proyecto.getPoligono() == null) {
			return Collections.emptyList();
		}

		List<List<String>> area = new ArrayList<>();
		Coordinate[] coordinates = proyecto.getPoligono().getExteriorRing().getCoordinates();
		for (Coordinate coordinate : coordinates) {
			double[] geo = TerraUtils.toGeo(coordinate.x, coordinate.y);
			if (geo == null || geo.length < 2) {
				continue;
			}

			String lon = String.format(Locale.US, "%.6f", geo[0]);
			String lat = String.format(Locale.US, "%.6f", geo[1]);
			area.add(List.of(lon, lat));
		}

		return area;
	}

}
