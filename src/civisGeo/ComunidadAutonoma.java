package civisGeo;
import java.util.HashMap;
import java.util.Map;

public class ComunidadAutonoma implements EntidadTerritorial {
    private final String name;
    private final Map<String, Provincia> provincias;

    public ComunidadAutonoma(String _name) {
        this.name = _name;
        this.provincias = new HashMap<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void addProvincia(Provincia p) {
        provincias.put(p.getName(), p);
    }

    public Provincia findProvincia(String _p) {
        Provincia p = provincias.get(_p);
        if (p == null) throw new IllegalArgumentException("Provincia '" + _p + "' no encontrada.");
        return p;
    }
}