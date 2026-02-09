package civisEconomy;

import java.util.*;

public class EntidadBancaria {
    private final String nombre;
    private final String codigoBanco;
    private final String swiftCode;
    private Double reservasLiquidas;
    private final Double encajeLegal; // % mínimo que el banco no puede tocar
    
    // Hilos seguros: varios cajeros o apps pueden acceder a la vez
    private Map<String, Cliente> clientes;
    private Map<String, CuentaBancaria> cuentas;
    private List<Transaccion> libroMayor;
    private List<Sucursal> sucursales;

    public EntidadBancaria(String nombre, String codigoBanco, String swiftCode, double encaje) {
        this.nombre = nombre;
        this.codigoBanco = codigoBanco;
        this.swiftCode = swiftCode;
        this.encajeLegal = 0.0; // Por defecto, el banco no tiene reservas líquidas
        this.reservasLiquidas = 0.0;
        this.clientes = new HashMap<>();
        this.cuentas = new HashMap<>();
        this.libroMayor = new ArrayList<>();
        this.sucursales = new ArrayList<>();
    }

    // Getters

    public String getNombre() {
        return nombre;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public Double getEncajeLegal() {
        return encajeLegal;
    }

    public Double getReservasLiquidas() {
        return reservasLiquidas;
    }

    public Map<String, Cliente> getClientes() {
        return clientes;
    }

    public Cliente getCliente(String dni) {
        Cliente c = clientes.get(dni);
        if(c != null) {
            return c;
        } else {
            throw new NoSuchElementException("No se encontró un cliente con DNI: " + dni);
        }
    }

    public Map<String, CuentaBancaria> getCuentas() {
        return cuentas;
    }

    public CuentaBancaria getCuenta(String numeroCuenta) {
        CuentaBancaria c = cuentas.get(numeroCuenta);
        if(c != null) {
            return c;
        } else {
            throw new NoSuchElementException("No se encontró una cuenta con número: " + numeroCuenta);
        }
    }

    public List<Transaccion> getLibroMayor() {
        return libroMayor;
    }

    public List<Sucursal> getSucursales() {
        return sucursales;
    }

    public List<Sucursal> getSucursalesPorMunicipio(String codigoPostal) {
        List<Sucursal> resultado = new ArrayList<>();
        for(Sucursal s : sucursales) {
            if(s.getMunicipio().getCodigoPostal().equals(codigoPostal)) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // Operaciones

    public Boolean agregarCliente(Cliente cliente) {
        if(cliente != null && !clientes.containsKey(cliente.getDni())) {
            clientes.put(cliente.getDni(), cliente);
            return true;
        }
        return false; // El cliente ya existe o es nulo
    }

    public Boolean bajaCliente(String dni) {
        if(dni != null) {
            clientes.remove(dni);
            return true;
        }
        return false;
    }

    public Cliente buscarCliente(String dni) {
        return clientes.get(dni);
    }

    public Boolean registrarTransaccion(Transaccion transaccion) {
        if(transaccion != null) {
            libroMayor.add(transaccion);
            return true;
        }
        return false; // La transacción es nula
    }

    public Boolean agregarCuenta(CuentaBancaria cuenta) {
        if(cuenta != null && !cuentas.containsKey(cuenta.getNumeroCuenta())) {
            cuentas.put(cuenta.getNumeroCuenta(), cuenta);
            return true;
        }
        return false; // La cuenta ya existe o es nula
    }

    public Boolean bajaCuenta(String numeroCuenta) {
        if(numeroCuenta != null) {
            cuentas.remove(numeroCuenta);
            return true;
        }
        return false;
    }

    public Boolean agregarSucursal(Sucursal sucursal) {
        if(sucursal != null && !sucursales.contains(sucursal)) {
            sucursales.add(sucursal);
            return true;
        }
        return false; // La sucursal ya existe o es nula
    }

    public Boolean eliminarSucursal(Sucursal sucursal, Sucursal sucursalAlternativa) {
        if(sucursal != null) {
            // Antes de eliminar, reasignamos clientes y cuentas a la sucursal alternativa
            for(Cliente c : sucursal.getClientesAsignados().values()) {
                sucursalAlternativa.asignarCliente(c);
            }
            for(CuentaBancaria cb : sucursal.getCuentasAsociadas()) {
                sucursalAlternativa.getCuentasAsociadas().add(cb);
            }
            return sucursales.remove(sucursal);
        }
        return false; // La sucursal es nula
    }
}