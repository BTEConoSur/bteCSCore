package com.bteconosur.db.model;

import java.time.Instant;
import java.util.UUID;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DateUtils;
import com.bteconosur.db.util.InteractionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "interaction")
/**
 * Entidad que representa una interacción temporal del sistema.
 * Se utiliza para solicitudes, componentes y datos auxiliares con expiración.
 */
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaction", nullable = false)
    private Long id;

    @Column(name = "uuid_player", columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID playerId;

    @Column(name = "id_proyecto", columnDefinition = "CHAR(6)")
    private String projectId;

    @Column(name = "poligono")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Polygon poligono;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_key", length = 50, nullable = false)
    private InteractionKey interactionKey;

    @Column(name = "channel_id", length = 32)
    private Long channelId;

    @Column(name = "message_id", length = 32)
    private Long messageId;

    @Column(name = "component_id", length = 100)
    private String componentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    public Interaction() {
    }

    public Interaction(UUID playerId, String projectId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(playerId, projectId, null, null, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(String projectId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(null, projectId, null, null, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(UUID playerId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(playerId, null, null, null, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(UUID playerId, Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(playerId, null, messageId, componentId, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(String projectId, Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(null, projectId, messageId, componentId, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(null, null, null, null, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this(null, null, messageId, componentId, interactionKey, createdAt, expiresAt, payloadJson);
    }

    public Interaction(UUID playerId, String projectId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(playerId, projectId, null, null, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(String projectId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(null, projectId, null, null, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(UUID playerId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(playerId, null, null, null, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(UUID playerId, Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(playerId, null, messageId, componentId, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(String projectId, Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(null, projectId, messageId, componentId, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(null, null, null, null, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt) {
        this(null, null, messageId, componentId, interactionKey, createdAt, expiresAt, null);
    }

    public Interaction(UUID playerId, String projectId, Long messageId, String componentId, InteractionKey interactionKey, Instant createdAt, Instant expiresAt, String payloadJson) {
        this.playerId = playerId;
        this.projectId = projectId;
        this.interactionKey = interactionKey;
        this.messageId = messageId;
        this.componentId = componentId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.payloadJson = payloadJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Polygon getPoligono() {
        return poligono;
    }

    public void setPoligono(Polygon poligono) {
        this.poligono = poligono;
    }

    public InteractionKey getInteractionKey() {
        return interactionKey;
    }

    public void setInteractionKey(InteractionKey interactionKey) {
        this.interactionKey = interactionKey;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    /**
     * Indica si la interacción se encuentra expirada.
     *
     * @return {@code true} si la fecha de expiración ya pasó.
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(DateUtils.instantOffset());
    }

    /**
     * Limpia completamente el payload JSON almacenado.
     */
    public void clearPayload() {
        this.payloadJson = null;
    }

    /**
     * Reemplaza el payload serializando un mapa de datos a JSON.
     *
     * @param data datos a serializar.
     */
    public void setPayloadData(Map<String, Object> data) {
        try {
            this.payloadJson = new ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            ConsoleLogger.error("Error serializando payload: ", e);
        }
    }

    /**
     * Obtiene el payload como mapa deserializado.
     *
     * @return mapa de datos del payload, o un mapa vacío si no existe o falla la deserialización.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPayloadData() {
        if (payloadJson == null || payloadJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return new ObjectMapper().readValue(payloadJson, Map.class);
        } catch (Exception e) {
            ConsoleLogger.error("Error deserializando payload: ", e);
            return new HashMap<>();
        }
    }

    /**
     * Obtiene un valor puntual del payload por clave.
     *
     * @param key clave del valor buscado.
     * @return valor asociado a la clave, o {@code null} si no existe.
     */
    public Object getPayloadValue(String key) {
        return getPayloadData().get(key);
    }

    /**
     * Agrega o reemplaza un valor dentro del payload.
     *
     * @param key clave a insertar o actualizar.
     * @param value valor a guardar.
     */
    public void addPayloadValue(String key, Object value) {
        Map<String, Object> data = getPayloadData();
        data.put(key, value);
        setPayloadData(data);
    }

}
