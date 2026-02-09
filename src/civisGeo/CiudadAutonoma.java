package civisGeo;
import java.util.HashMap;
import java.util.Map;

public class CiudadAutonoma implements EntidadTerritorial, ConCodigoPostal
{
    private final String name;
    private final Map<String, Localidad> localidades;
    private final String cp;

    public CiudadAutonoma(String _name, String _cp) {
        this.name = _name;
        this.cp = _cp;
        this.localidades = new HashMap<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCodigoPostal() {
        return this.cp;
    }

    public void addLocalidad(Localidad l) {
        localidades.put(l.getName(), l);
    }

    public Localidad findLocalidad(String _l) {
        return localidades.get(_l);
    }
}