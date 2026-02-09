package civisGeo;

public class Edificio {
    private int id;
    private Direccion direccion;
    private String uso; // Residencial, Comercial, Dotacional...
    private int anioConstruccion;

    // Podríamos añadir coordenadas si estuvieran en census.db
    // private double latitud;
    // private double longitud;

    public Edificio(int id, Direccion direccion) {
        this.id = id;
        this.direccion = direccion;
    }

    public Edificio(int id, Direccion direccion, String uso, int anioConstruccion) {
        this.id = id;
        this.direccion = direccion;
        this.uso = uso;
        this.anioConstruccion = anioConstruccion;
    }

    public int getId() {
        return id;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public String getUso() {
        return uso;
    }

    public int getAnioConstruccion() {
        return anioConstruccion;
    }

    @Override
    public String toString() {
        return "Edificio @ " + direccion.toString();
    }
}
