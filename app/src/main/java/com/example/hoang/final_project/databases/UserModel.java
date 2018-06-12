package com.example.hoang.final_project.databases;

import java.util.List;

/**
 * Created by hoang on 5/22/2018.
 */

public class UserModel {
    public String id;
    public String image;
    public String name;
    List<String> groupIDList;


    public UserModel(String id, String image, String name) {
        this.id = id;
        this.image = image;
        this.name = name;
    }
}
