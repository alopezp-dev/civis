package civisCitizen;

import civisGeo.Edificio;
import civisGeo.Localidad;
import civisGeo.Provincia;
import civisGeo.Pais;

public class Persona 
{
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String dni;
    private Edificio address;
    private Localidad city;
    private Provincia province;
    private Pais country;
    private String postalCode;
    private String phoneNumber;
    private String email;
    private String dateOfBirth;
    private String placeOfBirth;
    private String nationality;
    private Character gender;
    private Integer maritalStatus;

    public Persona(String name, String firstSurname, String secondSurname, String dni, Edificio address, 
        Localidad city, Provincia province, Pais country, String postalCode, String phoneNumber, 
        String email, String dateOfBirth, String placeOfBirth, String nationality, Character gender, 
        Integer maritalStatus)
    {
        this.name = name;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.dni = dni;
        this.address = address;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.nationality = nationality;
        this.gender = gender;
        this.maritalStatus = maritalStatus;
    }   

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstSurname() {
        return firstSurname;
    }

    public void setFirstSurname(String firstSurname) {
        this.firstSurname = firstSurname;
    }

    public String getSecondSurname() {
        return secondSurname;
    }

    public void setSecondSurname(String secondSurname) {
        this.secondSurname = secondSurname;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Edificio getAddress() {
        return address;
    }

    public void setAddress(Edificio address) {
        this.address = address;
    }

    public Localidad getCity() {
        return city;
    }

    public void setCity(Localidad city) {
        this.city = city;
    }

    public Provincia getProvince() {
        return province;
    }

    public void setProvince(Provincia province) {
        this.province = province;
    }

    public Pais getCountry() {
        return country;
    }

    public void setCountry(Pais country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Character getGender() {
        return gender;
    }

    public void setGender(Character gender) {
        this.gender = gender;
    }

    public Integer getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Integer maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", firstSurname=" + firstSurname + ", secondSurname=" + secondSurname
                + ", dni=" + dni + ", address=" + address + ", city=" + city + ", province=" + province
                + ", country=" + country + ", postalCode=" + postalCode + ", phoneNumber=" + phoneNumber
                + ", email=" + email + ", dateOfBirth=" + dateOfBirth + ", placeOfBirth=" + placeOfBirth
                + ", nationality=" + nationality + ", gender=" + gender + ", maritalStatus=" + maritalStatus + "]";
    }

}