package civisGeo;

import java.util.HashMap;
import java.util.Map;

public class Localidad {
    private final String name;
    private final String codigoPostal;
    private int numHabitantes;
    private Map<String, Calle> calles;

    public Localidad(String _name, String _codigoPostal, int nH) {
        this.name = _name;
        this.codigoPostal = _codigoPostal;
        this.numHabitantes = nH;
        this.calles = new HashMap<>();
    }

    public String getName() {
        return this.name;
    }

    public String getCodigoPostal() {
        return this.codigoPostal;
    }

    public int getNumHabitantes() {
        return this.numHabitantes;
    }

    public void increaseNumHabitantes(int _amount) {
        this.numHabitantes += _amount;
    }

    public void addCalle(Calle c) {
        calles.put(c.getNombre(), c);
    }

    public Calle getCalle(String nombre) {
        return calles.get(nombre);
    }

    public Map<String, Calle> getCalles() {
        return calles;
    }
}
