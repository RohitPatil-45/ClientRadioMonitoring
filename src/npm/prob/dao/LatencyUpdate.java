/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import npm.prob.datasource.Datasource;
import npm.prob.main.NodeStatusLatencyMonitoring;

public class LatencyUpdate implements Runnable {

    Connection connection = null;
    PreparedStatement preparedStatement = null;
    PreparedStatement preparedStatement2 = null;
    ResultSet resultSet = null;
    String sql = null;

    public void run() {
        //System.out.println("Start LatencyUpdate");
        System.out.println("Start mars_radio_health_monitoring update");
        while (true) {
            try {
                Thread.sleep(4000);
            } catch (Exception expp) {
                System.out.println("Exception In Thread Sleep BatchUpdate :" + expp);
            }

            try {
                NodeStatusLatencyMonitoring.client_radio_health_update_temp.clear();
                NodeStatusLatencyMonitoring.client_radio_health_update_temp.addAll(NodeStatusLatencyMonitoring.client_radio_health_update);
                NodeStatusLatencyMonitoring.client_radio_health_update.clear();
            } catch (Exception e) {
                System.out.println("Excpetion in try catch mars_radio_health_monitoring=" + e);
            }

            try {
                connection = Datasource.getConnection();

                sql = "UPDATE mars_radio_health_monitoring SET noiseLevel=?,powerSupply=?,receivedSignal=?,txPowerMeasurement=?,internalTemperature=?,"
                        + " eventTimestamp=? WHERE clientRadioId=? and mrId=?";
                preparedStatement = connection.prepareStatement(sql);
                for (int i = 0; i < NodeStatusLatencyMonitoring.client_radio_health_update_temp.size(); i++) {
                    try {
                        preparedStatement.setDouble(1, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getNoiseLevel());
                        preparedStatement.setDouble(2, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getPowerSupply());
                        preparedStatement.setDouble(3, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getReceivedSignal());
                        preparedStatement.setDouble(4, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getTxPowerMeasurement());
                        preparedStatement.setDouble(5, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getInternalTemperature());
                        preparedStatement.setTimestamp(6, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getEventTimestamp());
                        preparedStatement.setString(7, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getClientRadioId());
                        preparedStatement.setString(8, NodeStatusLatencyMonitoring.client_radio_health_update_temp.get(i).getMrId());
                        preparedStatement.addBatch();
                      //  System.out.println("updated IP :" + NodeStatusLatencyMonitoring.latency_update_temp.get(i).getDevice_ip());
                       // System.out.println("updated values  :" + NodeStatusLatencyMonitoring.latency_update_temp.get(i).getAvg_response());
                        if (i == 1000) {
                            int[] count = preparedStatement.executeBatch();

                            System.out.println("UPDATE mars_radio_health_monitoring inside: " + count.length);
                            preparedStatement = null;
                            preparedStatement = connection.prepareStatement(sql);
                        }

                        //System.out.println(dateFormat.format(in_startdate));
                    } catch (Exception e) {
                        System.out.println("Exception in mars_radio_health_monitoring=" + e);
                    }
                }
                int[] count = preparedStatement.executeBatch();

               System.out.println("##UPDATE mars_radio_health_monitoring parameters Count: " + count.length);
            } catch (Exception exp) {
                System.out.println("--$$$$$Exception In Batch Update " + exp);
            } finally {
                try {

                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }

                } catch (Exception ep) {
                    System.out.println("Exception1111Insweertr in update==== " + ep);
                }
            }

        }

    }
}
