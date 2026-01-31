package com.bteconosur.db.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuration")
public class Configuration {
    @Id
    @Column(name = "uuid_player", columnDefinition = "CHAR(36)", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID uuid;

    @Column(name = "general_global_chat_on_join")
    private Boolean generalGlobalChatOnJoin;

    @Column(name = "general_simultaneous_notifications")
    private Boolean generalSimultaneousNotifications;

    @Column(name = "general_pais_border")
    private Boolean generalPaisBorder;

    @Column(name = "reviewer_ds_notifications")
    private Boolean reviewerDsNotifications;

    @Column(name = "manager_ds_notifications")
    private Boolean managerDsNotifications;

    @OneToOne
    @MapsId
    @JoinColumn(name = "uuid")
    private Player player;

    public Configuration() {
    }

    public Configuration(Player player) {
        this.uuid = player.getUuid();
        this.player = player;
        this.generalGlobalChatOnJoin = false;
        this.reviewerDsNotifications = false;
        this.managerDsNotifications = false;
        this.generalSimultaneousNotifications = false;
        this.generalPaisBorder = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Boolean getGeneralGlobalChatOnJoin() {
        return generalGlobalChatOnJoin;
    }

    public void setGeneralGlobalChatOnJoin(Boolean generalGlobalChatOnJoin) {
        this.generalGlobalChatOnJoin = generalGlobalChatOnJoin;
    }

    public Boolean getGeneralSimultaneousNotifications() {
        return generalSimultaneousNotifications;
    }

    public void setGeneralSimultaneousNotifications(Boolean generalSimultaneousNotifications) {
        this.generalSimultaneousNotifications = generalSimultaneousNotifications;
    }

    public Boolean getReviewerDsNotifications() {
        return reviewerDsNotifications;
    }

    public void setReviewerDsNotifications(Boolean reviewerDsNotifications) {
        this.reviewerDsNotifications = reviewerDsNotifications;
    }

    public Boolean getManagerDsNotifications() {
        return managerDsNotifications;
    }

    public void setManagerDsNotifications(Boolean managerDsNotifications) {
        this.managerDsNotifications = managerDsNotifications;
    }

    public Boolean getGeneralPaisBorder() {
        return generalPaisBorder;
    }

    public void setGeneralPaisBorder(Boolean generalPaisBorder) {
        this.generalPaisBorder = generalPaisBorder;
    }

    public void toggleGeneralGlobalChatOnJoin() {
        this.generalGlobalChatOnJoin = !this.generalGlobalChatOnJoin;
    }

    public void toggleGeneralSimultaneousNotifications() {
        this.generalSimultaneousNotifications = !this.generalSimultaneousNotifications;
    }

    public void toggleReviewerDsNotifications() {
        this.reviewerDsNotifications = !this.reviewerDsNotifications;
    }

    public void toggleManagerDsNotifications() {
        this.managerDsNotifications = !this.managerDsNotifications;
    }

    public void toggleGeneralPaisBorder() {
        this.generalPaisBorder = !this.generalPaisBorder;
    }

}
