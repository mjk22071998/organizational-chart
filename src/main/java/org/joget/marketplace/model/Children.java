package org.joget.marketplace.model;

import java.util.List; // Import List from java.util
import org.joget.directory.model.Employment;
import org.joget.directory.model.User;

public class Children {

    private String name;
    private String title;
    private String className;
    private List<Children> children; // Define the children field
    private User hodUser;
    private Employment hod;
    // Getter and setter methods for name, title, className, and children

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Children> getChildren() {
        return children;
    }

    public void setChildren(List<Children> children) {
        this.children = children;
    }

    public User getHodUser() {
        return hodUser;
    }

    public void setHodUser(User hodUser) {
        this.hodUser = hodUser;
    }

    public Employment getHod() {
        return hod;
    }

    public void setHod(Employment hod) {
        this.hod = hod;
    }
}
