/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.model;

import java.sql.Timestamp;

/**
 *
 * @author Kratos
 */
public class MarsRadioHealthHistoryModel {

    private String mrId;

    private String mrName;

    private String clientRadioId;

    private String clientRadioName;
    
    private String clientRadioStatus;

    private double noiseLevel;
    private double powerSupply;
    private double internalTemperature;
    private double txPowerMeasurement;
    private double receivedSignal;

    private Timestamp eventTimestamp;

    public String getMrId() {
        return mrId;
    }

    public void setMrId(String mrId) {
        this.mrId = mrId;
    }
    

    public String getMrName() {
        return mrName;
    }

    public void setMrName(String mrName) {
        this.mrName = mrName;
    }

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

    public double getNoiseLevel() {
        return noiseLevel;
    }

    public void setNoiseLevel(double noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public double getPowerSupply() {
        return powerSupply;
    }

    public void setPowerSupply(double powerSupply) {
        this.powerSupply = powerSupply;
    }

    public double getInternalTemperature() {
        return internalTemperature;
    }

    public void setInternalTemperature(double internalTemperature) {
        this.internalTemperature = internalTemperature;
    }

    public double getTxPowerMeasurement() {
        return txPowerMeasurement;
    }

    public void setTxPowerMeasurement(double txPowerMeasurement) {
        this.txPowerMeasurement = txPowerMeasurement;
    }

    public double getReceivedSignal() {
        return receivedSignal;
    }

    public void setReceivedSignal(double receivedSignal) {
        this.receivedSignal = receivedSignal;
    }

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getClientRadioStatus() {
        return clientRadioStatus;
    }

    public void setClientRadioStatus(String clientRadioStatus) {
        this.clientRadioStatus = clientRadioStatus;
    }
    
    

}
