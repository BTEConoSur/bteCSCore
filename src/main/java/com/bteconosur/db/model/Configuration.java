package com.bteconosur.db.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bteconosur.core.config.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Column(name = "general_label_border")
    private Boolean generalLabelBorder;

    @Column(name = "reviewer_ds_notifications")
    private Boolean reviewerDsNotifications;

    @Column(name = "manager_ds_notifications")
    private Boolean managerDsNotifications;

    @Enumerated(EnumType.STRING) 
    @Column(name = "lang", nullable = false)
    private Language lang;

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
        this.generalLabelBorder = false;
        this.lang = Language.SPANISH;
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

    public Boolean getGeneralLabelBorder() {
        return generalLabelBorder;
    }

    public void setGeneralLabelBorder(Boolean generalLabelBorder) {
        this.generalLabelBorder = generalLabelBorder;
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

    public Language getLang() {
        return lang;
    }

    public void setLang(Language lang) {
        this.lang = lang;
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

    public void toggleGeneralLabelBorder() {
        this.generalLabelBorder = !this.generalLabelBorder;
    }

}
