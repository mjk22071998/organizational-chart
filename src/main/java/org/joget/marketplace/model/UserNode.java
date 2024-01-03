/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.joget.marketplace.model;

/**
 *
 * @author nabila
 */
public class UserNode {
    private String id;
    private String name;
    private String title;
    private String pid; // Parent ID

    public UserNode() {
    }

    public UserNode(String id, String name, String title, String pid) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.pid = pid;
    }

    // Getter and setter methods for the fields

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
    
}
