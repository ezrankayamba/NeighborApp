package tz.co.nezatech.neighborapp.model;

import java.io.Serializable;

public class Contact implements Serializable {
    private String msisdn, name;
    private boolean selected, registered;

    public Contact() {
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Contact other = (Contact) obj;
        if (!other.getMsisdn().equals(getMsisdn())) return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format(name + " " + msisdn);
    }
}
