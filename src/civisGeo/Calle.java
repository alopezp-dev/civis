package civisGeo;

import java.util.HashMap;
import java.util.Map;

public class Calle {
    private int id;
    private String codVia;
    private String nombre;
    private String tipoVia; // 'CL', 'AV', etc.
    private Map<Integer, Edificio> edificios;

    public Calle(int id, String codVia, String nombre, String tipoVia) {
        this.id = id;
        this.codVia = codVia;
        this.nombre = nombre;
        this.tipoVia = tipoVia;
        this.edificios = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getCodVia() {
        return codVia;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoVia() {
        return tipoVia;
    }

    public void addEdificio(Edificio e) {
        edificios.put(e.getId(), e);
    }

    public Edificio getEdificio(int id) {
        return edificios.get(id);
    }

    public Map<Integer, Edificio> getEdificios() {
        return edificios;
    }

    @Override
    public String toString() {
        return TipoVia.getNombreCompleto(tipoVia) + " " + nombre;
    }
}
