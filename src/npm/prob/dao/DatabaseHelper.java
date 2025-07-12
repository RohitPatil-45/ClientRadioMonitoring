/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

import npm.prob.datasource.Datasource;
import npm.prob.main.NodeStatusLatencyMonitoring;
import npm.prob.model.ClientRadioModel;
import npm.prob.model.NodeMasterModel;
import npm.prob.model.NodeStausModel;

/**
 *
 * @author NPM
 */
public class DatabaseHelper {

    public static void main(String[] args) {
        DatabaseHelper helper = new DatabaseHelper();
        helper.getNodeData();
    }

    public HashMap<String, ClientRadioModel> getNodeData() {
        Connection connection = null;
        Statement st1 = null;
        ResultSet rs = null;

        Statement st2 = null;
        ResultSet rs2 = null;

        PreparedStatement preparedStatement = null;
        PreparedStatement pstInsert = null;

        HashMap<String, ClientRadioModel> mapNodeData = new HashMap();

        try {
            connection = Datasource.getConnection();
            st1 = connection.createStatement();
            // String query = "select node.DEVICE_IP,node.DEVICE_NAME,node.DEVICE_TYPE,node.GROUP_NAME,node.COMPANY,node.LOCATION,node.DISTRICT,node.STATE,node.ZONE,parm.LATENCY_HISTORY,parm.LATENCY_THRESHOLD,mon.NODE_STATUS FROM ADD_NODE node JOIN NODE_PARAMETER parm ON node.DEVICE_IP=parm.DEVICE_IP JOIN node_monitoring mon ON node.DEVICE_IP=mon.NODE_IP  WHERE parm.MONITORING='yes' ORDER BY node.ID ";
            // String query = "select node.DEVICE_IP,node.DEVICE_NAME,node.DEVICE_TYPE,node.GROUP_NAME,node.COMPANY,node.LOCATION,node.DISTRICT,node.STATE,node.ZONE,parm.LATENCY_HISTORY,parm.LATENCY_THRESHOLD,mon.NODE_STATUS FROM ADD_NODE node JOIN NODE_PARAMETER parm ON node.DEVICE_IP=parm.DEVICE_IP JOIN node_monitoring mon ON node.DEVICE_IP=mon.NODE_IP  WHERE parm.MONITORING='yes' ORDER BY node.ID ";
            String query = "SELECT \n"
                    + "    hvid, \n"
                    + "    hvnamn, \n"
                    + "    hvnamn2, \n"
                    + "    hvserienr, \n"
                    + "    hvmanagementadr, \n"
                    + "    hvplid, \n"
                    + "    plnamn, \n"
                    + "    servicetypeid,\n"
                    + "	 serviceid \n"
                    + "FROM netadmin.hv \n"
                    + "JOIN netadmin.monitoring_service ON servicehvid = hvid \n"
                    + "LEFT JOIN netadmin.platser ON plid = hvplid \n"
                    + "WHERE hvfab IN ('9','23') \n"
                    + "AND servicetypeid IN ('13', '27', '57') AND hvnamn In('R2121')";
            // + " AND plnamn='canaris'";

            rs = st1.executeQuery(query);
            while (rs.next()) {
                //NodeMasterModel node = new NodeMasterModel();
                ClientRadioModel hv = new ClientRadioModel();

                String hvid = rs.getString(1);

                hv.setHvid(rs.getString(1));
                hv.setHvnamn(rs.getString(2));
                hv.setHvnamn2(rs.getString(3));
                hv.setHvserienr(rs.getString(4));
                hv.setHvmanagementadr(rs.getString(5));
                hv.setHvplid(rs.getString(6));
                hv.setPlnamn(rs.getString(7));
                hv.setServicetypeid(rs.getString(8));
                hv.setServiceid(rs.getString(9));

                mapNodeData.put(hvid, hv);
                NodeStatusLatencyMonitoring.latency_map.put(hvid, "Low");
                //NodeProbMonitoring.deviceStatusMap.put(device_ip, rs.getString(12));

                //TO DO:Rohit
                //Insert if not present ( in mars_radio_health_monitoring table  Default value- 0 value bydefaul and status Low, status Up
                try {

                    String queryText = "SELECT * FROM mars_radio_health_monitoring WHERE clientRadioName = ? and clientRadioId = ?";
                    preparedStatement = connection.prepareStatement(queryText);
                    preparedStatement.setString(1, rs.getString(2));
                    preparedStatement.setString(2, rs.getString(3));
                    rs2 = preparedStatement.executeQuery();
                    if (!rs2.next()) {

                        String query2 = "INSERT INTO mars_radio_health_monitoring (Internal_temperature_status,Noise_level_status,Power_supply_voltage_status,\n"
                                + "Received_Signal_status,Tx_Power_measurement_status,clientRadioId,clientRadioName,internalTemperature,mrId,mrName,\n"
                                + "noiseLevel,powerSupply,receivedSignal,txPowerMeasurement,Notes,eventTimestamp,clientRadioStatus) \n"
                                + "VALUES ('Low', 'Low', 'Low', 'Low', 'Low',?, ?, '0', ?, ?, '0', '0', '0', '0','', ?, 'Up')";
                        pstInsert = connection.prepareStatement(query2);
                        pstInsert.setString(1, rs.getString(3));
                        pstInsert.setString(2, rs.getString(2));
                        pstInsert.setString(3, rs.getString(5));
                        pstInsert.setString(4, rs.getString(2));
                        pstInsert.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

                        pstInsert.executeUpdate();

                    }

                } catch (Exception e) {
                    System.out.println("Exception in insertion in mars_radio_health_monitoring:" + e);
                } finally {
                    if (preparedStatement != null) {
                        try {
                            preparedStatement.close();
                        } catch (SQLException e) {
                            //System.out.println(e.getMessage());
                        }
                    }
                    if (rs2 != null) {
                        try {
                            rs2.close();
                        } catch (SQLException e) {
                            //System.out.println(e.getMessage());
                        }
                    }
                    if (pstInsert != null) {
                        try {
                            pstInsert.close();
                        } catch (SQLException e) {
                            //System.out.println(e.getMessage());
                        }
                    }

                }

            }

        } catch (Exception ex) {
            System.out.println("Exception node read:" + ex);
        } finally {
            if (st1 != null) {
                try {
                    st1.close();
                } catch (SQLException e) {
                    //System.out.println(e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //System.out.println(e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    //System.out.println(e.getMessage());
                }
            }
        }
        System.out.println("Insertion completed mars_radio_health_monitoring");
        System.out.println("Node Map Data:" + mapNodeData);

        //System.out.println("StatusMap:" + NodeStatusLatencyMonitoring.deviceStatusMap);
        return mapNodeData;
    }

    public void latencyThreshold(String device_ip, int latency_threshold, float avg_responce, String latency_status) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;
        StatusLog log = new StatusLog();
        try {
            threshold_log_con = Datasource.getConnection();
            Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO latency_threshold_history (NODE_IP,LATENCY_THRESHOLD,LATENCY_VAL,LATENCY_STATUS,EVENT_TIMESTAMP) VALUES (?,?,?,?,?)");
            pstmtThresholdLog.setString(1, device_ip);
            pstmtThresholdLog.setInt(2, latency_threshold);
            pstmtThresholdLog.setFloat(3, avg_responce);
            pstmtThresholdLog.setString(4, latency_status);
            pstmtThresholdLog.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmtThresholdLog.executeUpdate();

            if (latency_status.equalsIgnoreCase("High")) {
                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;

                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
            }

        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE node_monitoring SET LATENCY=?,LATENCY_THRESHOLD=?,LATENCY_STATUS=?,LATENCY_TIMESTAMP=? WHERE NODE_IP=? ");
            pstmtThresholdUpdate.setFloat(1, avg_responce);
            pstmtThresholdUpdate.setInt(2, latency_threshold);
            pstmtThresholdUpdate.setString(3, latency_status);
            pstmtThresholdUpdate.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmtThresholdUpdate.setString(5, device_ip);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert cpu log exp:" + exp);
            }
        }
    }

    public void insertStatusTatusTimeDiff(String device_ip, Timestamp down_event_time, Timestamp up_event_time, String difference_time, Long totalsecofdate) {
        PreparedStatement preparedStatement1 = null;
        Connection connection = null;
        try {
            connection = Datasource.getConnection();
            preparedStatement1 = connection.prepareStatement("INSERT INTO STATUS_DIFF_TIME (DEVICE_IP,DOWN_EVENT_TIME,UP_EVENT_TIME,EVENT_DIFFERENCE,EVENT_DIFFERENCE_SECOND) VALUES (?,?,?,?,?)");
            preparedStatement1.setString(1, device_ip);
            preparedStatement1.setTimestamp(2, down_event_time);
            preparedStatement1.setTimestamp(3, up_event_time);
            preparedStatement1.setString(4, difference_time);
            preparedStatement1.setLong(5, totalsecofdate);
            preparedStatement1.executeUpdate();
            System.out.println("insert uptime webserive:" + device_ip);
        } catch (Exception e) {
            System.out.println(device_ip + "inser node status time Exception:" + e);
        } finally {
            try {
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }
    }

    public void insertIntoMarsRadioHealthHistory(String hvnamn, String hvnamn2, String hvmanagementadr, double rssi, double noise, double temperature, double txPower, double powerSupplyVoltage) {
        PreparedStatement preparedStatement1 = null;
        Connection connection = null;
        try {
            connection = Datasource.getConnection();
            preparedStatement1 = connection.prepareStatement("INSERT INTO mars_radio_health_history (clientRadioId, clientRadioName, eventTimestamp, internalTemperature, mrId,"
                    + " mrName, noiseLevel, powerSupply, receivedSignal, txPowerMeasurement) VALUES (?,?,?,?,?,?,?,?,?,?)");
            preparedStatement1.setString(1, hvnamn2);
            preparedStatement1.setString(2, hvnamn);
            preparedStatement1.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement1.setDouble(4, temperature);
            preparedStatement1.setString(5, hvmanagementadr);
            preparedStatement1.setString(6, hvnamn);
            preparedStatement1.setDouble(7, noise);
            preparedStatement1.setDouble(8, powerSupplyVoltage);
            preparedStatement1.setDouble(9, rssi);
            preparedStatement1.setDouble(10, txPower);
            preparedStatement1.executeUpdate();

        } catch (Exception e) {
            System.out.println(hvnamn + "inserting in Mars_radio_health_history Exception:" + e);
        } finally {
            try {
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }
    }

    public void insertIntoEventLog(String deviceID, String deviceName, String eventMsg, int severity, String serviceName, Timestamp evenTimestamp, String netadmin_msg, String isAffected, String problem) {
        PreparedStatement preparedStatement1 = null;
        Connection connection = null;
        try {
            connection = Datasource.getConnection();
            preparedStatement1 = connection.prepareStatement("INSERT INTO event_log (device_id, device_name, service_name, event_msg, netadmin_msg, severity,"
                    + " event_timestamp, acknowledgement_status, isAffected, Problem_Clear) VALUES (?,?,?,?,?,?,?,?,?,?)");
            preparedStatement1.setString(1, deviceID);
            preparedStatement1.setString(2, deviceName);
            preparedStatement1.setString(3, serviceName);
            preparedStatement1.setString(4, eventMsg);
            preparedStatement1.setString(5, netadmin_msg);
            preparedStatement1.setInt(6, severity);
            preparedStatement1.setTimestamp(7, evenTimestamp);
            preparedStatement1.setBoolean(8, false);
            preparedStatement1.setString(9, isAffected);
            preparedStatement1.setString(10, problem);

            preparedStatement1.executeUpdate();

        } catch (Exception e) {
            System.out.println(deviceID + "inserting in Mars_radio_health_history Exception:" + e);
        } finally {
            try {
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }
    }

    public void temperatureThresholdLog(String device_ip, String deviceName, int latency_threshold, double avg_responce, String latency_status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;
        StatusLog log = new StatusLog();
        String clientRadioID = device_ip.split("_")[1];
        String mrId = device_ip.split("_")[0];

        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO mars_radio_health_threshold_log (clientRadioId,clientRadioName,eventTimestamp,mrId,mrName,"
                    + "status,type,value) VALUES (?,?,?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, clientRadioID);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setTimestamp(3, logDateTime);
            pstmtThresholdLog.setString(4, mrId);
            pstmtThresholdLog.setString(5, deviceName);
            pstmtThresholdLog.setString(6, latency_status);
            pstmtThresholdLog.setString(7, "internaltemperature");
            pstmtThresholdLog.setDouble(8, avg_responce);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE mars_radio_health_monitoring SET Internal_temperature_status=?,internalTemperature=?,eventTimestamp=? WHERE clientRadioId=? "
                    + "and mrId=?");
            pstmtThresholdUpdate.setString(1, latency_status);
            pstmtThresholdUpdate.setDouble(2, avg_responce);
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setString(4, clientRadioID);
            pstmtThresholdUpdate.setString(5, mrId);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void noiseLevelThresholdLog(String device_ip, String deviceName, int latency_threshold, double avg_responce, String latency_status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;
        StatusLog log = new StatusLog();
        String clientRadioID = device_ip.split("_")[1];
        String mrId = device_ip.split("_")[0];

        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO mars_radio_health_threshold_log (clientRadioId,clientRadioName,eventTimestamp,mrId,mrName,"
                    + "status,type,value) VALUES (?,?,?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, clientRadioID);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setTimestamp(3, logDateTime);
            pstmtThresholdLog.setString(4, mrId);
            pstmtThresholdLog.setString(5, deviceName);
            pstmtThresholdLog.setString(6, latency_status);
            pstmtThresholdLog.setString(7, "noiselevel");
            pstmtThresholdLog.setDouble(8, avg_responce);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE mars_radio_health_monitoring SET Noise_level_status=?,noiseLevel=?,eventTimestamp=? WHERE clientRadioId=? "
                    + "and mrId=?");
            pstmtThresholdUpdate.setString(1, latency_status);
            pstmtThresholdUpdate.setDouble(2, avg_responce);
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setString(4, clientRadioID);
            pstmtThresholdUpdate.setString(5, mrId);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void powerSupplyThresholdLog(String device_ip, String deviceName, int latency_threshold, double avg_responce, String latency_status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;
        StatusLog log = new StatusLog();
        String clientRadioID = device_ip.split("_")[1];
        String mrId = device_ip.split("_")[0];

        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO mars_radio_health_threshold_log (clientRadioId,clientRadioName,eventTimestamp,mrId,mrName,"
                    + "status,type,value) VALUES (?,?,?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, clientRadioID);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setTimestamp(3, logDateTime);
            pstmtThresholdLog.setString(4, mrId);
            pstmtThresholdLog.setString(5, deviceName);
            pstmtThresholdLog.setString(6, latency_status);
            pstmtThresholdLog.setString(7, "powersupplyvoltage");
            pstmtThresholdLog.setDouble(8, avg_responce);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE mars_radio_health_monitoring SET Power_supply_voltage_status=?,powerSupply=?,eventTimestamp=? WHERE clientRadioId=? "
                    + "and mrId=?");
            pstmtThresholdUpdate.setString(1, latency_status);
            pstmtThresholdUpdate.setDouble(2, avg_responce);
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setString(4, clientRadioID);
            pstmtThresholdUpdate.setString(5, mrId);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void rssiThresholdLog(String device_ip, String deviceName, int latency_threshold, double avg_responce, String latency_status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;
        StatusLog log = new StatusLog();
        String clientRadioID = device_ip.split("_")[1];
        String mrId = device_ip.split("_")[0];

        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO mars_radio_health_threshold_log (clientRadioId,clientRadioName,eventTimestamp,mrId,mrName,"
                    + "status,type,value) VALUES (?,?,?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, clientRadioID);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setTimestamp(3, logDateTime);
            pstmtThresholdLog.setString(4, mrId);
            pstmtThresholdLog.setString(5, deviceName);
            pstmtThresholdLog.setString(6, latency_status);
            pstmtThresholdLog.setString(7, "rssi");
            pstmtThresholdLog.setDouble(8, avg_responce);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE mars_radio_health_monitoring SET Received_Signal_status=?,receivedSignal=?,eventTimestamp=? WHERE clientRadioId=? "
                    + "and mrId=?");
            pstmtThresholdUpdate.setString(1, latency_status);
            pstmtThresholdUpdate.setDouble(2, avg_responce);
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setString(4, clientRadioID);
            pstmtThresholdUpdate.setString(5, mrId);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void txPowerThresholdLog(String device_ip, String deviceName, int latency_threshold, double avg_responce, String latency_status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;
        StatusLog log = new StatusLog();
        String clientRadioID = device_ip.split("_")[1];
        String mrId = device_ip.split("_")[0];

        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO mars_radio_health_threshold_log (clientRadioId,clientRadioName,eventTimestamp,mrId,mrName,"
                    + "status,type,value) VALUES (?,?,?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, clientRadioID);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setTimestamp(3, logDateTime);
            pstmtThresholdLog.setString(4, mrId);
            pstmtThresholdLog.setString(5, deviceName);
            pstmtThresholdLog.setString(6, latency_status);
            pstmtThresholdLog.setString(7, "txpower");
            pstmtThresholdLog.setDouble(8, avg_responce);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE mars_radio_health_monitoring SET Tx_Power_measurement_status=?,txPowerMeasurement=?,eventTimestamp=? WHERE clientRadioId=? "
                    + "and mrId=?");
            pstmtThresholdUpdate.setString(1, latency_status);
            pstmtThresholdUpdate.setDouble(2, avg_responce);
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setString(4, clientRadioID);
            pstmtThresholdUpdate.setString(5, mrId);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void updateMonitoringInstanceStatus(String hvid, String instanceKey, int severity) {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = Datasource.getConnection();
            pst = con.prepareStatement("UPDATE netadmin.monitoring_instance_status SET isseverity = ? WHERE isid = (SELECT mi.instanceid FROM netadmin.monitoring_instance mi INNER JOIN\n"
                    + "netadmin.monitoring_service ms ON mi.instanceserviceid = ms.serviceid\n"
                    + "WHERE ms.servicehvid = ? AND mi.instancekey = ?)");
            pst.setInt(1, severity);
            pst.setString(2, hvid);
            pst.setString(3, instanceKey);

            pst.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("update monitoring_instance_status exception normal:" + e);
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

}
