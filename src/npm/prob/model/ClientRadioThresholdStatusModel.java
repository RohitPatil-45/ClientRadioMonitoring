/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.model;

/**
 *
 * @author Kratos
 */
public class ClientRadioThresholdStatusModel {

    private String clientRadioId;
    private String clientRadioName;
    private String Internal_temperature_status;
    private String Noise_level_status;

    private String Power_supply_voltage_status;
    private String Received_Signal_status;
    private String Tx_Power_measurement_status;

    public String getClientRadioId() {
        return clientRadioId;
    }

    public void setClientRadioId(String clientRadioId) {
        this.clientRadioId = clientRadioId;
    }

    public String getClientRadioName() {
        return clientRadioName;
    }

    public void setClientRadioName(String clientRadioName) {
        this.clientRadioName = clientRadioName;
    }

    public String getInternal_temperature_status() {
        return Internal_temperature_status;
    }

    public void setInternal_temperature_status(String Internal_temperature_status) {
        this.Internal_temperature_status = Internal_temperature_status;
    }

    public String getNoise_level_status() {
        return Noise_level_status;
    }

    public void setNoise_level_status(String Noise_level_status) {
        this.Noise_level_status = Noise_level_status;
    }

    public String getPower_supply_voltage_status() {
        return Power_supply_voltage_status;
    }

    public void setPower_supply_voltage_status(String Power_supply_voltage_status) {
        this.Power_supply_voltage_status = Power_supply_voltage_status;
    }

    public String getReceived_Signal_status() {
        return Received_Signal_status;
    }

    public void setReceived_Signal_status(String Received_Signal_status) {
        this.Received_Signal_status = Received_Signal_status;
    }

    public String getTx_Power_measurement_status() {
        return Tx_Power_measurement_status;
    }

    public void setTx_Power_measurement_status(String Tx_Power_measurement_status) {
        this.Tx_Power_measurement_status = Tx_Power_measurement_status;
    }
    
    
    

}
