package tz.co.nezatech.neighborapp.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    String name;
    long id;
    List<Member> members;

    public Group() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
}
