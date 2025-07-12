/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npm.prob.model;

import java.io.Serializable;

/**
 *
 * @author Kratos
 */
public class ClientRadioModel implements Serializable{
    
     private static final long serialVersionUID = -2264642949863409860L;
     
     private String hvid;
     
     private String hvnamn;
     
     private String hvnamn2;
     
     private String hvserienr;
     
     private String hvmanagementadr;
     
     private String hvplid;
     
     private String plnamn;
     
     private String servicetypeid;
     
     private String serviceid;

    public String getHvid() {
        return hvid;
    }

    public void setHvid(String hvid) {
        this.hvid = hvid;
    }

    public String getHvnamn() {
        return hvnamn;
    }

    public void setHvnamn(String hvnamn) {
        this.hvnamn = hvnamn;
    }

    public String getHvnamn2() {
        return hvnamn2;
    }

    public void setHvnamn2(String hvnamn2) {
        this.hvnamn2 = hvnamn2;
    }

    public String getHvserienr() {
        return hvserienr;
    }

    public void setHvserienr(String hvserienr) {
        this.hvserienr = hvserienr;
    }

    public String getHvmanagementadr() {
        return hvmanagementadr;
    }

    public void setHvmanagementadr(String hvmanagementadr) {
        this.hvmanagementadr = hvmanagementadr;
    }

    public String getHvplid() {
        return hvplid;
    }

    public void setHvplid(String hvplid) {
        this.hvplid = hvplid;
    }

    public String getPlnamn() {
        return plnamn;
    }

    public void setPlnamn(String plnamn) {
        this.plnamn = plnamn;
    }

    public String getServicetypeid() {
        return servicetypeid;
    }

    public void setServicetypeid(String servicetypeid) {
        this.servicetypeid = servicetypeid;
    }

    public String getServiceid() {
        return serviceid;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }
 
     
}
