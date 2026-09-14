package com.bteconosur.db.model;

import java.util.ArrayList;
import java.util.List;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "tour")
public class Tour {

    @Id
    @Column(name = "id", length = 30)
    private String id; 

    @ManyToOne
    @JoinColumn(name = "id_pais")
    private Pais pais;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<TourStop> paradas = new ArrayList<>();

    public Tour() {}

    public Tour(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public List<TourStop> getParadas() {
        return paradas;
    }

    public void setParadas(List<TourStop> paradas) {
        this.paradas = paradas;
    }

    public void addParada(TourStop parada) {
        paradas.add(parada);
        parada.setTour(this);
    }

    public void removeParada(TourStop parada) {
        paradas.remove(parada);
        parada.setTour(null);
    }
    
    public boolean hasParada(String id) {
        for (TourStop parada : paradas) {
            if (parada.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkOrden(int orden) {  
        return orden >= 1 && orden <= paradas.size() + 1;
    }

    public TourStop getParada(int orden) {
        for (TourStop parada : paradas) {
            if (parada.getOrden() == orden) {
                return parada;
            }
        }
        return null;
    }

    public TourStop getParada(String id) {
        for (TourStop parada : paradas) {
            if (parada.getId().equalsIgnoreCase(id)) {
                return parada;
            }
        }
        return null;
    }

    public List<String> getDescription(Language language) {
        String key = "tours." + id + ".desc";
        List<String> translated = LanguageHandler.getTextList(language, key);
        if (!translated.isEmpty()) return translated;
        return LanguageHandler.getTextList(Language.getDefault(), key);
    }

}
