package civisGeo;

public class Direccion {
    private Calle calle;
    private int numero;
    private String letra; // 'A', 'B', etc. (opcional)
    private String codigoPostal;

    public Direccion(Calle calle, int numero) {
        this.calle = calle;
        this.numero = numero;
    }

    public Direccion(Calle calle, int numero, String letra, String codigoPostal) {
        this.calle = calle;
        this.numero = numero;
        this.letra = letra;
        this.codigoPostal = codigoPostal;
    }

    public Calle getCalle() {
        return calle;
    }

    public int getNumero() {
        return numero;
    }

    public String getLetra() {
        return letra;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    @Override
    public String toString() {
        String dir = calle.toString() + ", " + numero;
        if (letra != null && !letra.isEmpty()) {
            dir += " " + letra;
        }
        if (codigoPostal != null && !codigoPostal.isEmpty()) {
            dir += " (" + codigoPostal + ")";
        }
        return dir;
    }
}
