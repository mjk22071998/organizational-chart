package org.joget.marketplace.model;

import java.util.List;
import org.joget.directory.model.User;

public class DepartmentNode {
    
    private String id;
    private String name;
    private String pid;
    private String hodName;
    private String title;
    private List<User> users; // Corrected the type to List<User>

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private List<DepartmentNode> children; // Add a list of children

    public String getHodName() {
        return hodName;
    }

    public void setHodName(String hodName) {
        this.hodName = hodName;
    }
   

    // Getter and setter methods for id, name, pid, and children

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public List<DepartmentNode> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentNode> children) {
        this.children = children;
    }

    public void addChild(DepartmentNode child) {
        children.add(child);
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
