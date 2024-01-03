package org.joget.marketplace.model;

import java.util.List;

public class Parent {
    
    private String name;
    private String title;
    private List<Children> children;

    // Getter and setter methods for name, title, and children

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

    public List<Children> getChildren() {
        return children;
    }

    public void setChildren(List<Children> children) {
        this.children = children;
    }
}