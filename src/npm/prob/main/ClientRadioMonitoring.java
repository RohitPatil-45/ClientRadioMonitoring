/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.main;

import npm.prob.dao.LatencyLog;
import npm.prob.dao.LatencyUpdate;

/**
 *
 * @author Rohit
 */
public class ClientRadioMonitoring {

    public static void main(String[] args) {
        System.out.println("Start Device status latency history 26Dec24 3am- Working hours-8am to 7pm@");
        
        /* try {
             MailConfig mailConfig = new MailConfig();
             mailConfig.loadConfig();
        } catch (Exception e) {
            System.out.println("Exception occured in mail config =" + e);
        }*/

        try {
            Thread t2 = null;
            t2 = new Thread(new NodeStatusLatencyMonitoring());
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception NodeProbMonitoring:" + e);
        }

        //client_radio_health Update
        try {
            Thread t2 = null;
            t2 = new Thread(new LatencyUpdate());
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception LatencyUpdate:" + e);
        }

        // client_radio_health_log
        try {
            Thread t2 = null;
            t2 = new Thread(new LatencyLog());
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception LatencyUpdate:" + e);
        }
        
        
          //Status Update
//        try {
//            Thread t2 = null;
//            t2 = new Thread(new StatusUpdate());
//            t2.start();
//        } catch (Exception e) {
//            System.out.println("Exception LatencyUpdate:" + e);
//        }
//
//        // //Status Log
//        try {
//            Thread t2 = null;
//            t2 = new Thread(new StatusLog());
//            t2.start();
//        } catch (Exception e) {
//            System.out.println("Exception LatencyUpdate:" + e);
//        }

    }

}
