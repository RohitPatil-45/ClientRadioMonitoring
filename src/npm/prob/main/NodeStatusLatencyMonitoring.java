/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import npm.prob.dao.DatabaseHelper;
import npm.prob.datasource.Datasource;
import npm.prob.model.ClientRadioModel;
import npm.prob.model.LatencyModel;
import npm.prob.model.MarsRadioHealthHistoryModel;
import npm.prob.model.NodeMasterModel;
import npm.prob.model.NodeStausModel;

//Features
//1) Hisotry of 5 paramter
//2) update live 5 parameter
//Threshol
//3) Threshold log
//4) Thrwshold update
//5) Event log- Threshol
//Status
//1)update status -client radio id(when change)
//2)Maintain Log -client radio status
//3) Event log- -client radio status
/**
 *
 * @author NPM
 */
public class NodeStatusLatencyMonitoring implements Runnable {

    public static HashMap<String, ClientRadioModel> mapNodeData2 = null;
    public static HashMap<String, NodeMasterModel> mapNodeData = null;
    public static ArrayList<LatencyModel> latency_list = null;
    public static ArrayList<LatencyModel> latency_list_temp = null;
    public static ArrayList<LatencyModel> latency_update = null;
    public static ArrayList<LatencyModel> latency_update_temp = null;
    public static HashMap latency_map = null;

    public static ArrayList<MarsRadioHealthHistoryModel> client_radio_health_list = null;
    public static ArrayList<MarsRadioHealthHistoryModel> client_radio_health_list_temp = null;

    public static ArrayList<MarsRadioHealthHistoryModel> client_radio_health_update = null;
    public static ArrayList<MarsRadioHealthHistoryModel> client_radio_health_update_temp = null;
    //public static HashMap<String, String> deviceStatusMap = null;

    public static ArrayList<NodeStausModel> statusUpdateList = null;
    public static ArrayList<NodeStausModel> statusUpdateListTemp = null;
    public static ArrayList<NodeStausModel> statusLogList = null;
    public static ArrayList<NodeStausModel> statusLogListTemp = null;

    public static HashMap temperatureThresholdMap = null;
    public static HashMap noiseThresholdMap = null;
    public static HashMap rssiThresholdMap = null;
    public static HashMap powerSupplyThresholdMap = null;
    public static HashMap txPowerThresholdMap = null;

    public static HashMap clientRadioStatusMap = null;

    public static int temperatureThresholdParam;
    public static int noiseThresholdParam;
    public static int powerSupplyThresholdParam;
    public static int rssiThresholdParam;
    public static int txPowerThresholdParam;
    //To DO:Rohit

    public void run() {

        temperatureThresholdMap = new HashMap<String, String>();
        noiseThresholdMap = new HashMap<String, String>();
        rssiThresholdMap = new HashMap<String, String>();
        powerSupplyThresholdMap = new HashMap<String, String>();
        txPowerThresholdMap = new HashMap<String, String>();
        clientRadioStatusMap = new HashMap<String, String>();

        latency_update = new ArrayList<>();
        latency_update_temp = new ArrayList<>();
        latency_list_temp = new ArrayList<>();
        latency_map = new HashMap();
        //  deviceStatusMap = new HashMap();

        statusUpdateList = new ArrayList<>();
        statusLogList = new ArrayList<>();

        statusUpdateListTemp = new ArrayList<>();
        statusLogListTemp = new ArrayList<>();

        //client Radio
        client_radio_health_list = new ArrayList<>();
        client_radio_health_list_temp = new ArrayList<>();
        client_radio_health_update = new ArrayList<>();
        client_radio_health_update_temp = new ArrayList<>();

        //Threshold status
        Connection xmlcon = null;
        try {

            xmlcon = Datasource.getConnection();

                String SQL = "select clientRadioId,mrId,clientRadioName,Internal_temperature_status,Noise_level_status,Power_supply_voltage_status,Received_Signal_status,"
                        + "Tx_Power_measurement_status,clientRadioStatus from mars_radio_health_monitoring";
            Statement customerRS = xmlcon.createStatement();
            ResultSet xmlrs = customerRS.executeQuery(SQL);
            while (xmlrs.next()) {
                // String deviceID = mr id + "_" + clientradio id;
                String deviceID = xmlrs.getString(2) + "_" + xmlrs.getString(1);

                noiseThresholdMap.put(deviceID, xmlrs.getString(5));
                temperatureThresholdMap.put(deviceID, xmlrs.getString(4));
                rssiThresholdMap.put(deviceID, xmlrs.getString(7));
                powerSupplyThresholdMap.put(deviceID, xmlrs.getString(6));
                txPowerThresholdMap.put(deviceID, xmlrs.getString(8));

                clientRadioStatusMap.put(deviceID, xmlrs.getString(9));
                //To DO:Rohit  : All threshold status and clientradiostatus
            }

        } catch (Exception ex) {
            //System.out.println("create error " + ex);
        } finally {
            if (xmlcon != null) {
                try {
                    xmlcon.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());

                }
            }
        }

        //Threshold Parameeter
        //TO DO Rohit: Read paramter from DB
        try {

            xmlcon = Datasource.getConnection();

            String SQL = "select noiselevel,powersupplyvoltage,rssi,internalTemperature,txPowerMeasurement from threshold_parameter";
            Statement customerRS = xmlcon.createStatement();
            ResultSet xmlrs = customerRS.executeQuery(SQL);
            while (xmlrs.next()) {
                temperatureThresholdParam = xmlrs.getInt(4);
                noiseThresholdParam = xmlrs.getInt(1);
                powerSupplyThresholdParam = xmlrs.getInt(2);
                rssiThresholdParam = xmlrs.getInt(3);
                txPowerThresholdParam = xmlrs.getInt(5);

            }

            System.out.println("Temperature Threhsold = " + temperatureThresholdParam);
            System.out.println("noise Threhsold = " + noiseThresholdParam);
            System.out.println("power Threhsold = " + powerSupplyThresholdParam);
            System.out.println("rssi Threhsold = " + rssiThresholdParam);
            System.out.println("tx Threhsold = " + txPowerThresholdParam);

        } catch (Exception ex) {
            //System.out.println("create error " + ex);
        } finally {
            if (xmlcon != null) {
                try {
                    xmlcon.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());

                }
            }
        }

        DatabaseHelper helper = new DatabaseHelper();
        mapNodeData2 = helper.getNodeData();
        System.out.println(mapNodeData2.size() + ":NodeProbMonitoring:" + mapNodeData2);
        Iterator<Map.Entry<String, ClientRadioModel>> itr = mapNodeData2.entrySet().iterator();

        ExecutorService executor = null;
        Runnable worker = null;
        executor = Executors.newFixedThreadPool(mapNodeData2.size());

        while (itr.hasNext()) {
            try {
                Map.Entry<String, ClientRadioModel> entry = itr.next();
                ClientRadioModel clientradiomodel = entry.getValue();

                worker = null;
                worker = new ClientRadioPHPMon(clientradiomodel);
                executor.execute(worker);

            } catch (Exception e) {
                System.err.println("Exceptionn: " + e.getMessage());
            }
        }

//        while (itr.hasNext()) {
        //            try {
        //                Map.Entry<String, ClientRadioModel> entry = itr.next();
        //                // System.out.println("Key = " + entry.getKey()
        //                //         + ", Value = " + entry.getValue());
        //                m = m + 1;
        //                inner_list.add(entry.getKey());
        //                if (m % 1 == 0) {
        //                    outer_list.add(inner_list.toString());
        //                    inner_list.clear();
        //                }
        //            } catch (Exception e) {
        //                System.out.println("Exception:" + e);
        //            }
        //
        //        }
        //
        //        if ((inner_list.size()) != 0) {
        //            outer_list.add(inner_list);
        //        }
        //        System.out.println("outer list:" + outer_list);
        //        System.out.println("Thread size:" + outer_list.size());
        //        int pool_sizee5 = outer_list.size();
        //        System.out.println("Thread Pool Size " + pool_sizee5);
        //
        //        ExecutorService executor = null;
        //        Runnable worker = null;
        //        executor = null;
        //        executor = Executors.newFixedThreadPool(pool_sizee5);
        //        Iterator out_itr = outer_list.iterator();
        //        int thread_count = 0;
        //
        //        while (out_itr.hasNext()) {
        //            String a = out_itr.next().toString();
        //            String b = a.substring(1, a.length() - 1);
        //            List<String> myList = null;
        //            myList = new ArrayList<String>(Arrays.asList(b.split(",")));
        //            // System.out.println("list1:" + myList);
        //            try {
        //                thread_count++;
        //                System.out.println("Thread Count:" + thread_count);
        //                
        //                worker = null;
        //                worker = new NodeMon(myList);
        //                executor.execute(worker);
        //                Thread.sleep(500);
        //                //System.out.println(thread_count + "th Thread started ");
        //            } catch (Exception e) {
        //                System.err.println("Exceptionn: " + e.getMessage());
        //            }
        //
        //        }
    }

}
