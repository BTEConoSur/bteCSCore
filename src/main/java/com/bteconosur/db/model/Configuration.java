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

    @Column(name = "reviewer_toggle_test")
    private Boolean reviewerToggleTest;

    @Column(name = "manager_toggle_test")
    private Boolean managerToggleTest;

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
        this.reviewerToggleTest = false;
        this.managerToggleTest = false;
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

    public Boolean getReviewerToggleTest() {
        return reviewerToggleTest;
    }

    public void setReviewerToggleTest(Boolean reviewerToggleTest) {
        this.reviewerToggleTest = reviewerToggleTest;
    }

    public Boolean getManagerToggleTest() {
        return managerToggleTest;
    }

    public void setManagerToggleTest(Boolean managerToggleTest) {
        this.managerToggleTest = managerToggleTest;
    }

    public void toggleGeneralGlobalChatOnJoin() {
        this.generalGlobalChatOnJoin = !this.generalGlobalChatOnJoin;
    }

    public void toggleReviewerTest() {
        this.reviewerToggleTest = !this.reviewerToggleTest;
    }

    public void toggleManagerTest() {
        this.managerToggleTest = !this.managerToggleTest;
    }
}
