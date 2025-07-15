/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import npm.prob.dao.DatabaseHelper;
import npm.prob.datasource.Datasource;
import npm.prob.model.ClientRadioModel;
import npm.prob.model.MarsRadioHealthHistoryModel;
import org.json.JSONObject;

/**
 *
 * @author Kratos
 */
public class ClientRadioPHPMon implements Runnable {

    ClientRadioModel radio = null;
    DatabaseHelper db = new DatabaseHelper();
    boolean simulation = true;
    String deviceType = "CLIENT_RADIO";

    ClientRadioPHPMon(ClientRadioModel m) {
        this.radio = m;
    }

    @Override
    public void run() {

        while (true) {

            String cient_radio_id = radio.getHvnamn2();
            String deviceID = radio.getHvmanagementadr() + "_" + radio.getHvnamn2();
            String deviceName = radio.getHvnamn();
            String hvid = radio.getHvid();
            String isAffected = "0";
            String problem = "problem";
            String serviceId = "";

            StringBuilder outputBuilder = new StringBuilder();
            ProcessBuilder builder = null;

            if (simulation) {
                builder = new ProcessBuilder("php", "C:Simulation\\ClientRadioMonitoring.php", radio.getHvmanagementadr(),
                        radio.getHvnamn2());
            } else {
                builder = new ProcessBuilder("php", "C:Canaris\\ClientRadio\\ClientRadioMonitoring.php", radio.getHvmanagementadr(),
                        radio.getHvnamn2());
            }

            try {

                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("output json = " + output.toString());
                    JSONObject json = new JSONObject(output.toString());

                    double rssi = 0.0;
                    double noise = 0.0;
                    double temperature = 0.0;
                    double txPower = 0.0;
                    double powerSupplyVoltage = 0.0;
                    String router_status = "";

                    try {
                        rssi = json.getDouble("rssi");
                        noise = json.getDouble("noiselevel");
                        temperature = json.getDouble("internaltemperature");
                        txPower = json.getDouble("txpower");
                        powerSupplyVoltage = json.getDouble("powersupplyvoltage");
                        router_status = json.getString("clientRadioStatus");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exption_Output_Json : " + e);
                        rssi = 0.0;
                        noise = 0.0;
                        temperature = 0.0;
                        txPower = 0.0;
                        powerSupplyVoltage = 0.0;
                        router_status = "Down";
                    }

                    System.out.println("RSSI: " + rssi);
                    System.out.println("Noise Level: " + noise);
                    System.out.println("Internal Temp: " + temperature);
                    System.out.println("TX Power: " + txPower);
                    System.out.println("Voltage: " + powerSupplyVoltage);

                    //insert into mars_radio_health_history and update health parameters in Mars_radio_health_monitoring
                    try {

                        MarsRadioHealthHistoryModel model = new MarsRadioHealthHistoryModel();
                        model.setClientRadioId(cient_radio_id); //client radio id  e.g 0001001
                        model.setClientRadioName(radio.getHvnamn());
                        model.setEventTimestamp(new Timestamp(System.currentTimeMillis()));
                        model.setInternalTemperature(temperature);
                        model.setMrId(radio.getHvmanagementadr()); //mr_id  e.g 172.30.21.18:9003
                        model.setMrName(radio.getHvnamn());
                        model.setNoiseLevel(noise);
                        model.setPowerSupply(powerSupplyVoltage);
                        model.setReceivedSignal(rssi);
                        model.setTxPowerMeasurement(txPower);
                        model.setClientRadioStatus(router_status);

                        NodeStatusLatencyMonitoring.client_radio_health_list.add(model);
                        NodeStatusLatencyMonitoring.client_radio_health_update.add(model);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Threshold Monitoring start
                    checkTemperatureThreshold(temperature, deviceID, deviceName, hvid);
                    checkNoiseThreshold(noise, deviceID, deviceName, hvid);
                    checkPowerSupplyThreshold(powerSupplyVoltage, deviceID, deviceName, hvid);
                    checkRssiThreshold(rssi, deviceID, deviceName, hvid);
                    checkTxPowerThreshold(txPower, deviceID, deviceName, hvid);
                    //Threhsold Monitoring end

                    //Status Monitorinstart
                    String router_status_xml = NodeStatusLatencyMonitoring.clientRadioStatusMap.get(deviceID).toString();
                    String device_ip = deviceID;
                    String eventMsg1 = "";
                    String netadminMsg = "";

                    if (router_status == null || router_status_xml == null || router_status.equalsIgnoreCase(router_status_xml)) {
                        //  //System.out.println("********************Not Change Router Status****************");
                    } else {
                        Timestamp event_time = new Timestamp(System.currentTimeMillis());
                        if (router_status_xml.equalsIgnoreCase("Up") && router_status.equalsIgnoreCase("Down")) {
                            System.out.println("1st down:" + device_ip);
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Down1");

                        } else if (router_status_xml.equalsIgnoreCase("Down1") && router_status.equalsIgnoreCase("Down")) {
                            System.out.println("up to warrning:" + device_ip);
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Down2");
                            updateDeviceStatus(device_ip, "Warning", event_time);
//                                try {
//                                    Thread.sleep(2000);
//                                } catch (Exception e) {
//                                    //System.out.println("e:" + e);
//                                }
                        } else if (router_status_xml.equalsIgnoreCase("Down2") && router_status.equalsIgnoreCase("Down")) {
                            System.out.println("@@$$Down Device:" + device_ip);

                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Down3");
                            updateDeviceStatus(device_ip, "Down", event_time); // TO Do: mars_radio_health_monitoring : clientRadioStatus
                            updateHvServiceMode(device_ip, 1);
                            deviceStatusLog(device_ip, deviceName, "Down", event_time);
                            // TO Do: client_radio_status_log column( device_id,mr_id,client_radio_id,status,timestamp

                            //TO DO: insert into event Log
                            eventMsg1 = "Client radio : " + deviceName + " is Down";
                            netadminMsg = netadminMsg = deviceName + " is Down";;
                            isAffected = "1";
                            serviceId = "client_radio_status";
                            db.insertIntoEventLog(deviceID, deviceName, eventMsg1, 4, "ClientRadio Status", event_time, netadminMsg, isAffected, problem, serviceId, deviceType);

                        } else if (router_status_xml.equalsIgnoreCase("Down3") && router_status.equalsIgnoreCase("Down")) {
                            //    //System.out.println("%%%%%..Skip Down condition ");
                        } else if (router_status_xml.equalsIgnoreCase("Down3") && router_status.equalsIgnoreCase("Up")) {
                            System.out.println("Down to Up");
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Up");
                            updateDeviceStatus(device_ip, "Up", event_time);
                            updateHvServiceMode(device_ip, 0);
                            deviceStatusLog(device_ip, deviceName, "Up", event_time);
                            eventMsg1 = "Client radio : " + deviceName + " is Up";
                            netadminMsg = deviceName + " is Up";
                            isAffected = "0";
                            problem = "Cleared";
                            serviceId = "client_radio_status";
                            db.insertIntoEventLog(deviceID, deviceName, eventMsg1, 0, "ClientRadio Status", event_time, netadminMsg, isAffected, problem, serviceId, deviceType);
                            try {
                                StatusChangeDiff t22 = null;
                                t22 = new StatusChangeDiff();
                                t22.insertStatusDiff(device_ip, event_time);
                            } catch (Exception e) {
                                System.out.println("Uptime Thread Exception:" + e);
                            }

                        } else if (router_status_xml.equalsIgnoreCase("Down1") && router_status.equalsIgnoreCase("Up")) {
                            System.out.println("1st down then Up:" + device_ip);
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Up");
                            updateDeviceStatus(device_ip, "Up", event_time);
                            updateHvServiceMode(device_ip, 0);

                        } else if (router_status_xml.equalsIgnoreCase("Down2") && router_status.equalsIgnoreCase("Up")) {
                            System.out.println("2nd down Warning then Up:" + device_ip);;
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Up");
                            updateDeviceStatus(device_ip, "Up", event_time);
                            updateHvServiceMode(device_ip, 0);

                        } else if (router_status_xml.equalsIgnoreCase("Down") && router_status.equalsIgnoreCase("Up")) {

                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Up");
                            updateDeviceStatus(device_ip, "Up", event_time);
                            updateHvServiceMode(device_ip, 0);
                            deviceStatusLog(device_ip, deviceName, "Up", event_time);
                            eventMsg1 = "Client radio : " + deviceName + " is up";
                            netadminMsg = netadminMsg = deviceName + " is Up";;
                            isAffected = "0";
                            problem = "Cleared";
                            serviceId = "client_radio_status";
                            db.insertIntoEventLog(deviceID, deviceName, eventMsg1, 0, "ClientRadio Status", event_time, netadminMsg, isAffected, problem, serviceId, deviceType);

                            try {
                                StatusChangeDiff t22 = null;
                                t22 = new StatusChangeDiff();
                                t22.insertStatusDiff(device_ip, event_time);
                            } catch (Exception e) {
                                System.out.println("Uptime Thread Exception:" + e);
                            }
                        } else if (router_status_xml.equalsIgnoreCase("Warning") && router_status.equalsIgnoreCase("Up")) {
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Up");
                            updateDeviceStatus(device_ip, "Up", event_time);
                            updateHvServiceMode(device_ip, 0);
                            System.out.println("1st down then Up:" + device_ip);
                        } else if (router_status_xml.equalsIgnoreCase("Warning") && router_status.equalsIgnoreCase("Down")) {
                            NodeStatusLatencyMonitoring.clientRadioStatusMap.put(device_ip, "Down");
                            updateDeviceStatus(device_ip, "Down", event_time);
                            updateHvServiceMode(device_ip, 1);
                        } else {
                            //System.out.println(router_ipadress + "Else Condition*********************************** old:" + router_status_xml + ":New:" + router_status);
                        }

                    }
                    //status Monmmonitoring end

                } else {
                    System.err.println("PHP script failed.");
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientRadioPHPMon.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void checkTemperatureThreshold(double actual_value, String deviceID, String deviceName, String hvid) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = NodeStatusLatencyMonitoring.temperatureThresholdParam;
        String isAffected = "0";
        String problem = "problem";
        String serviceId = "internaltemperature";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = NodeStatusLatencyMonitoring.temperatureThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("Internal Temperature:High" + actual_value + " Internal Temperature value=" + threshold + " Internal Temperature status=" + "High" + " ip=" + deviceID);
                eventMsg = "Internal Temperature Threshold:High" + actual_value + " Internal Temperature value=" + threshold + " Internal Temprature status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: internaltemperature = " + actual_value;
                NodeStatusLatencyMonitoring.temperatureThresholdMap.put(deviceID, "High");
                DatabaseHelper db = new DatabaseHelper();
                db.temperatureThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                isAffected = "1";
                problem = "problem";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "internaltemperature", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
                db.updateMonitoringInstanceStatus(hvid, "internaltemperature", 5);
//                db.updateMarsThresholdStatus(deviceID, "High");// Update Thrshold Status = need understanding

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {

                NodeStatusLatencyMonitoring.temperatureThresholdMap.put(deviceID, "Low");
                DatabaseHelper db = new DatabaseHelper();
                db.temperatureThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);
                db.updateMonitoringInstanceStatus(hvid, "internaltemperature", 0);
                eventMsg = "Internal Temperature Threshold:Low" + actual_value + " Internal Temperature value=" + threshold + " Internal Temprature status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: internaltemperature = " + actual_value;
                isAffected = "0";
                problem = "Cleared";
                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 0, "internaltemperature", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
            }
        } catch (Exception e4) {
            System.out.println(" Internal Temperature Threshold:" + e4);
        }
    }

    public void checkNoiseThreshold(double actual_value, String deviceID, String deviceName, String hvid) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = NodeStatusLatencyMonitoring.noiseThresholdParam;
        String isAffected = "0";
        String problem = "problem";
        String serviceId = "noiselevel";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = NodeStatusLatencyMonitoring.noiseThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("Noise Level:High" + actual_value + " Noise level threshold value=" + threshold + " Noise level status=" + "High" + " ip=" + deviceID);
                eventMsg = "Noise Level Threshold:High" + actual_value + " Noise level threshold value=" + threshold + " Noise level status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: internaltemperature = " + actual_value;
                NodeStatusLatencyMonitoring.noiseThresholdMap.put(deviceID, "High");
                DatabaseHelper db = new DatabaseHelper();
                db.noiseLevelThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                db.updateMonitoringInstanceStatus(hvid, "noiselevel", 5);
                isAffected = "1";
                problem = "problem";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "noiselevel", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
//                db.updateMarsThresholdStatus(deviceID, "High");// Update Thrshold Status

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {

                NodeStatusLatencyMonitoring.noiseThresholdMap.put(deviceID, "Low");
                DatabaseHelper db = new DatabaseHelper();
                db.noiseLevelThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);
                db.updateMonitoringInstanceStatus(hvid, "noiselevel", 0);
                eventMsg = "Noise Level Threshold:Low" + actual_value + " Noise level threshold value=" + threshold + " Noise level status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: internaltemperature = " + actual_value;
                isAffected = "0";
                problem = "Cleared";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 0, "noiselevel", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType);
            }
        } catch (Exception e4) {
            System.out.println(" Noise Level Threshold:" + e4);
        }
    }

    public void checkPowerSupplyThreshold(double actual_value, String deviceID, String deviceName, String hvid) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = NodeStatusLatencyMonitoring.powerSupplyThresholdParam;
        String isAffected = "0";
        String problem = "problem";
        String serviceId = "powersupplyvoltage";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = NodeStatusLatencyMonitoring.powerSupplyThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("Power Supply :High" + actual_value + " Power Supply threshold value=" + threshold + " Power Supply status=" + "High" + " ip=" + deviceID);
                eventMsg = "Power Supply Threshold:High" + actual_value + " Power Supply threshold value=" + threshold + " Power Supply status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: powersupplyvoltage = " + actual_value;
                NodeStatusLatencyMonitoring.powerSupplyThresholdMap.put(deviceID, "High");
                DatabaseHelper db = new DatabaseHelper();
                db.powerSupplyThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                db.updateMonitoringInstanceStatus(hvid, "powersupplyvoltage", 5);
                isAffected = "1";
                problem = "problem";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "powersupplyvoltage", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
//                db.updateMarsThresholdStatus(deviceID, "High");// Update Thrshold Status

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {

                NodeStatusLatencyMonitoring.powerSupplyThresholdMap.put(deviceID, "Low");
                DatabaseHelper db = new DatabaseHelper();
                db.powerSupplyThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);
                db.updateMonitoringInstanceStatus(hvid, "powersupplyvoltage", 0);
                eventMsg = "Power Supply Threshold:Low" + actual_value + " Power Supply threshold value=" + threshold + " Power Supply status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: powersupplyvoltage = " + actual_value;
                isAffected = "0";
                problem = "Cleared";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 0, "powersupplyvoltage", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType);
            }
        } catch (Exception e4) {
            System.out.println(" Power Supply Threshold:" + e4);
        }
    }

    public void checkRssiThreshold(double actual_value, String deviceID, String deviceName, String hvid) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = NodeStatusLatencyMonitoring.rssiThresholdParam;
        String isAffected = "0";
        String problem = "problem";
        String serviceId = "rssi";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = NodeStatusLatencyMonitoring.rssiThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("rssi :High" + actual_value + " rssi threshold value=" + threshold + " rssi status=" + "High" + " ip=" + deviceID);
                eventMsg = "rssi Threshold:High" + actual_value + " rssi threshold value=" + threshold + " rssi status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: rssi = " + actual_value;
                NodeStatusLatencyMonitoring.rssiThresholdMap.put(deviceID, "High");
                DatabaseHelper db = new DatabaseHelper();
                db.rssiThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                db.updateMonitoringInstanceStatus(hvid, "rssi", 5);

                isAffected = "1";
                problem = "problem";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "rssi", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
//                db.updateMarsThresholdStatus(deviceID, "High");// Update Thrshold Status

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {

                NodeStatusLatencyMonitoring.rssiThresholdMap.put(deviceID, "Low");
                DatabaseHelper db = new DatabaseHelper();
                db.rssiThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);
                db.updateMonitoringInstanceStatus(hvid, "rssi", 0);
                eventMsg = "rssi Threshold:Low" + actual_value + " rssi threshold value=" + threshold + " rssi status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: rssi = " + actual_value;
                isAffected = "0";
                problem = "Cleared";
                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 0, "rssi", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log

            }
        } catch (Exception e4) {
            System.out.println(" rssi Threshold:" + e4);
        }
    }

    public void checkTxPowerThreshold(double actual_value, String deviceID, String deviceName, String hvid) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = NodeStatusLatencyMonitoring.txPowerThresholdParam;
        String isAffected = "0";
        String problem = "problem";
        String serviceId = "txpower";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = NodeStatusLatencyMonitoring.txPowerThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("Tx Power :High" + actual_value + " Tx Power value=" + threshold + " Tx Power status=" + "High" + " ip=" + deviceID);
                eventMsg = "Tx Power Threshold:High" + actual_value + " Tx Power threshold value=" + threshold + " Tx Power status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: txpower = " + actual_value;
                NodeStatusLatencyMonitoring.txPowerThresholdMap.put(deviceID, "High");
                DatabaseHelper db = new DatabaseHelper();
                db.txPowerThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                db.updateMonitoringInstanceStatus(hvid, "txpower", 5);
                isAffected = "1";
                problem = "problem";

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "txpower", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
//                db.updateMarsThresholdStatus(deviceID, "High");// Update Thrshold Status

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {

                NodeStatusLatencyMonitoring.txPowerThresholdMap.put(deviceID, "Low");
                DatabaseHelper db = new DatabaseHelper();
                db.txPowerThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);
                db.updateMonitoringInstanceStatus(hvid, "txpower", 0);

                isAffected = "0";
                problem = "Cleared";
                eventMsg = "Tx Power Threshold:Low" + actual_value + " Tx Power threshold value=" + threshold + " Tx Power status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Satel Combined online diagnostics polling: txpower = " + actual_value;
                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 0, "txpower", logDateTime, netadmin_msg, isAffected, problem, serviceId, deviceType); //Evrnt log
            }
        } catch (Exception e4) {
            System.out.println(" Tx Power Threshold:" + e4);
        }
    }

    public void updateDeviceStatus(String device_ip, String device_status, Timestamp eventTime) {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = Datasource.getConnection();
            pst = con.prepareStatement("UPDATE mars_radio_health_monitoring SET clientRadioStatus=?, eventTimestamp=?, clientRadioStatus_Generated_Time=?, "
                    + "clientRadioStatus_Cleared_Time=? WHERE clientRadioId=? "
                    + "and mrId=?");
            pst.setString(1, device_status);
            pst.setTimestamp(2, eventTime);
            pst.setTimestamp(3, device_status.equalsIgnoreCase("Down") ? eventTime : null);
            pst.setTimestamp(4, device_status.equalsIgnoreCase("Up") ? eventTime : null);
            pst.setString(5, device_ip.split("_")[1]);
            pst.setString(6, device_ip.split("_")[0]);

            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("UPDATE mars_radio_health_monitoring exception normal:" + e);
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void deviceStatusLog(String device_ip, String deviceName, String device_status, Timestamp eventTime) {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = Datasource.getConnection();
            pst = con.prepareStatement("insert into client_radio_status_log (device_name,mr_id,client_radio_id,status,timestamp) "
                    + "values (?,?,?,?,?)");
            pst.setString(1, deviceName);
            pst.setString(2, device_ip.split("_")[0]);
            pst.setString(3, device_ip.split("_")[1]);
            pst.setString(4, device_status);
            pst.setTimestamp(5, eventTime);

            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert client_radio_status_log exception normal:" + e);
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    private void updateHvServiceMode(String device_ip, int hvServiceMode) {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = Datasource.getConnection();
            pst = con.prepareStatement("UPDATE netadmin.hv SET hvservicemode=? WHERE hvmanagementadr=? "
                    + "and hvnamn2=?");
            pst.setInt(1, hvServiceMode);
            pst.setString(2, device_ip.split("_")[0]);
            pst.setString(3, device_ip.split("_")[1]);

            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("UPDATE hv exception normal:" + e);
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception exp) {
                System.out.println("update hv log exp:" + exp);
            }
        }
    }
}
