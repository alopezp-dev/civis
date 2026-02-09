package civisEconomy;

import civisGeo.Localidad;

import civisGeo.Edificio; 
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Sucursal {
    private final String codigoSucursal; // Formato "0001", "1234", etc.
    private final EntidadBancaria banco;
    private final Localidad municipio;
    private final Edificio edificio;
    private Integer numCajeros;
    private Map<String, Cliente> clientesAsignados;
    private List<CuentaBancaria> cuentasAsociadas;

    public Sucursal(EntidadBancaria banco, Localidad municipio, Edificio edificio) {
        // Asignar campos primero (necesario para generar código basado en municipio)
        this.banco = banco;
        this.municipio = municipio;
        this.edificio = edificio;
        this.codigoSucursal = generarCodigoSucursal();
        this.numCajeros = 0;
        this.clientesAsignados = new HashMap<>();
        this.cuentasAsociadas = new ArrayList<>();
    }

    private String generarCodigoSucursal() {
        // 1. Obtenemos el prefijo de la provincia (ej. "10" para Cáceres)
        String prefijoProvincia = municipio.getCodigoPostal().substring(0, 2);
        
        // 2. Contamos cuántas sucursales tiene ya el banco en esa provincia
        // Accedemos a la lista global del banco para que el número sea correlativo
        long correlativo = banco.getSucursales().stream()
            .filter(s -> s.getMunicipio().getCodigoPostal().startsWith(prefijoProvincia))
            .count() + 1;

        // 3. Formateamos el correlativo a 2 dígitos (ej. 1 -> "01")
        String sufijo = String.format("%02d", correlativo);
        
        // Resultado: "10" + "01" = "1001"
        return prefijoProvincia + sufijo;
    }

    // Getters
    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    public EntidadBancaria getBanco() {
        return banco;
    }

    public Localidad getMunicipio() {
        return municipio;
    }

    public Edificio getEdificio() {
        return edificio;
    }

    public Integer getNumCajeros() {
        return numCajeros;
    }

    public void setNumCajeros(Integer numCajeros) {
        this.numCajeros = numCajeros;
    }

    public Map<String, Cliente> getClientesAsignados() {
        return clientesAsignados;
    }

    public List<CuentaBancaria> getCuentasAsociadas() {
        return cuentasAsociadas;
    }

    public void asignarCliente(Cliente cliente) {
        clientesAsignados.put(cliente.getUsuario().getDni(), cliente);
    }

    public void crearCuentaBancaria(Cliente cliente, String divisa) {
        // Lógica para crear una cuenta bancaria y asociarla al cliente
        CuentaBancaria nuevaCuenta = new CuentaBancaria(banco.getCodigoBanco(), this.codigoSucursal, divisa);
        cuentasAsociadas.add(nuevaCuenta);
        cliente.agregarCuenta(nuevaCuenta);
    }

}