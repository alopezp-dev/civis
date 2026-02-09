package civisGeo;

import java.util.HashMap;
import java.util.Map;

public class Pais 
{
    private final String name;
    private final Map<String, EntidadTerritorial> divisiones;

    public Pais(String _name)
    {
        this.name = _name;
        divisiones = new HashMap<>();
    }

    public String getName()
    {
        return this.name;
    }

    public void addDivision(EntidadTerritorial c)
    {
        divisiones.put(c.getName(), c);
    }

    public EntidadTerritorial findComunidad(String _c)
    {
        EntidadTerritorial c = divisiones.get(_c);
        if(c != null) return c;
        else throw new Error("Comunidad no encontrada");
    }

}
