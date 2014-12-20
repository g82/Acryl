package com.gamepari.acryl.data;

/**
 * Created by seokceed on 2014-12-20.
 */
public class Playground {

    private int tag_num;
    private int inst_num;

    private String pay;
    private int trans_costs;

    private String instName;

    private String fullAddress;
    private String address1;
    private String address2;

    private boolean isChecked;

    public int getTag_num() {
        return tag_num;
    }

    public void setTag_num(int tag_num) {
        this.tag_num = tag_num;
    }

    public int getInst_num() {
        return inst_num;
    }

    public void setInst_num(int inst_num) {
        this.inst_num = inst_num;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public int getTrans_costs() {
        return trans_costs;
    }

    public void setTrans_costs(int trans_costs) {
        this.trans_costs = trans_costs;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(String instName) {
        this.instName = instName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(int isChecked) {
        this.isChecked = (isChecked == 1) ? true : false;
    }
}
