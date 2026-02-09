package civisGeo;
import java.util.HashMap;
import java.util.Map;

public class Provincia 
{
    private final String name;
    private final String prefijoPostal;
    private final Map<String, Localidad> localidades;

    public Provincia(String _name, String _prefijoPostal) 
    {
        this.name = _name;
        this.prefijoPostal = _prefijoPostal;
        this.localidades = new HashMap<>();
    }

    public String getName() { return this.name; }

    public String getPrefijoPostal() { return this.prefijoPostal; }

    public void addLocalidad(Localidad l) 
    {
        localidades.put(l.getName(), l);
    }

    public Localidad findLocalidad(String _l) 
    {
        return localidades.get(_l);
    }
}