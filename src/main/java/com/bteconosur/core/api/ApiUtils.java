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
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.TerraUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.util.Estado;
import com.bteconosur.db.util.PlaceholderUtils;

/**
 * Utilitario para operaciones relacionadas con la API web de BTE. Provee métodos auxiliares
 * para obtener credenciales, convertir datos de proyectos a formatos compatibles con la API,
 * y manipular streams de respuesta HTTP.
 */
public class ApiUtils {

	private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
	private static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    /**
     * Obtiene la descripción de un proyecto para ser utilizada en la API web.
     * Reemplaza placeholders con información del proyecto en el idioma por defecto.
     *
     * @param proyecto proyecto del cual obtener la descripción, o {@code null}.
     * @return descripción formateada del proyecto o cadena vacía si el proyecto es nulo.
     */
    private static String getWebDescription(Proyecto proyecto) {
        if (proyecto == null) return "";
		List<String> descriptionLines = new ArrayList<>();
		for (String line : LanguageHandler.getTextList(Language.getDefault(), "web-proyecto-description")) {
			descriptionLines.add(PlaceholderUtils.replaceDS(line, Language.getDefault(), proyecto));
		}
		return String.join(" | ", descriptionLines);
    }

	/**
	 * Obtiene el token de autenticación para la API web de BTE.
	 * Si el modo debug está activo, retorna el token debug del archivo de secretos.
	 * De lo contrario, retorna el token del país especificado.
	 *
	 * @param pais país del cual obtener el token, o {@code null}.
	 * @return token de autenticación o cadena vacía si no se puede obtener.
	 */
	public static String getToken(Pais pais) {
		if (config.getBoolean("web-debug-mode", false)) {
			return secret.getString("web-debug-token").trim();
		}
		if (pais == null || pais.getWebToken() == null) return "";
		return pais.getWebToken().trim();
	}

	/**
	 * Obtiene el identificador del equipo de construcción (buildTeamId o slug) para la API web de BTE.
	 * Si el modo debug está activo, retorna el ID debug del archivo de secretos.
	 * De lo contrario, retorna el ID del país especificado.
	 *
	 * @param pais país del cual obtener el ID, o {@code null}.
	 * @return identificador del equipo o cadena vacía si no se puede obtener.
	 */
	public static String getBuildTeamId(Pais pais) {
		if (config.getBoolean("web-debug-mode", false)) {
			return secret.getString("web-debug-id").trim();
		}
		if (pais == null || pais.getWebId() == null) return "";
		return pais.getWebId().trim();
	}

	/**
	 * Lee el contenido completo de un stream de entrada y lo convierte en una cadena.
	 * Utiliza codificación UTF-8 y cierra el stream automáticamente al finalizar.
	 *
	 * @param stream stream de entrada a leer, o {@code null}.
	 * @return contenido del stream como cadena, o cadena vacía si el stream es nulo.
	 * @throws IOException si ocurre un error al leer el stream.
	 */
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

	/**
	 * Convierte un proyecto en una solicitud de claim compatible con la API web de BTE.
	 * Incluye información del propietario, área del proyecto, estado, nombre y descripción.
	 * Las coordenadas se convierten de coordenadas de Minecraft a coordenadas geográficas.
	 *
	 * @param proyecto proyecto a convertir, o {@code null}.
	 * @return solicitud de claim con la información del proyecto o una solicitud vacía si el proyecto es nulo.
	 */
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

		return request;
	}

	/**
	 * Obtiene una referencia de usuario para el propietario del proyecto desde el archivo de secretos.
	 *
	 * @return referencia de usuario del propietario con su UUID.
	 */
	private static UserRef getOwnerUserRef() {
		String ownerId = secret.getString("web-owner-id", "").trim();
        UserRef userRef = new UserRef();
        userRef.setId(UUID.fromString(ownerId));
        return userRef;
	}

	/**
	 * Convierte el polígono de un proyecto de coordenadas de Minecraft a coordenadas geográficas
	 * en formato de lista de pares longitud-latitud (lon, lat) con precisión de 6 decimales.
	 *
	 * @param proyecto proyecto cuyo polígono será convertido, o {@code null}.
	 * @return lista de pares [lon, lat] o lista vacía si el proyecto o polígono es nulo.
	 */
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
