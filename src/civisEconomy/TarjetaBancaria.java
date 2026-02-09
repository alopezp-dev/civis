package civisEconomy;

public class TarjetaBancaria {
    private String numeroTarjeta;
    private String tipo; // Débito o Crédito
    private String fechaExpiracion;
    private String cvv;
    private CuentaBancaria cuentaAsociada;
    private Cliente titular;

    public TarjetaBancaria(CuentaBancaria cuenta, Cliente titular, String tipo) {
        this.cuentaAsociada = cuenta;
        this.titular = titular;
        this.tipo = tipo;
        this.numeroTarjeta = generarNumeroTarjeta();
        this.fechaExpiracion = generarFechaExpiracion();
        this.cvv = generarCVV();
    }

    private String generarNumeroTarjeta() {
        // Generar un número de tarjeta único (simplificado)
        return "4000" + String.format("%012d", (long)(Math.random() * 1_000_000_000_000L));
    }

    private String generarFechaExpiracion() {
        // Generar una fecha de expiración a 3 años vista (MM/AA)
        int mes = (int)(Math.random() * 12) + 1;
        int año = (int)(Math.random() * 3) + 24; // Asumiendo año actual es 2024
        return String.format("%02d/%02d", mes, año);
    }

    private String generarCVV() {
        // Generar un CVV de 3 dígitos
        return String.format("%03d", (int)(Math.random() * 1000));
    }
}
