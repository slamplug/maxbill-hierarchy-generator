package com.firstutility.utils.hierarchygenerator.model;

public class Accounts {

    private String energyCustomerNumber;
    private String telcoCustomerNumber;
    private String errorMessage;

    public Accounts() {}

    public Accounts(String energyCustomerNumber, String telcoCustomerNumber) {
        this.energyCustomerNumber = energyCustomerNumber;
        this.telcoCustomerNumber = telcoCustomerNumber;
    }

    public String getEnergyCustomerNumber() { return energyCustomerNumber; }

    public void setEnergyCustomerNumber(String energyCustomerNumber) { this.energyCustomerNumber = energyCustomerNumber; }

    public String getTelcoCustomerNumber() { return telcoCustomerNumber; }

    public void setTelcoCustomerNumber(String telcoCustomerNumber) { this.telcoCustomerNumber = telcoCustomerNumber; }

    public String getErrorMessage() { return errorMessage;
    }

    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "Accounts{" + "energyCustomerNumber='" + energyCustomerNumber + '\'' + ", "
                + "telcoCustomerNumber='" + telcoCustomerNumber
                + '\''
                + '}';
    }
}
