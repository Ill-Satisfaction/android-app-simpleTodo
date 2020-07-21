package com.example.myapplication;

import androidx.annotation.Nullable;

import java.util.List;

public class Collection {
    //fields
    private String title;
    private List<String> items;
    private MainActivity.FILES filename;
    private ItemsAdapter ia;

    //constructors
    public Collection (MainActivity.FILES fname, List<String> items) {
        this(fname, items, null);
    }

    public Collection (MainActivity.FILES fname, List<String> items, @Nullable  ItemsAdapter ia) {
        this.filename = fname;
        this.title = fname.toString();
        this.items = items;
        this.ia = ia;
    }

    //methods
    public String getTitle () {
        return this.title;
    }
    public void setTitle (String s) {
        this.title = s;
    }
    public List<String> getItems () {
        return this.items;
    }
    public void setItems (List<String> l) {
        this.items = l;
    }
    public MainActivity.FILES getFilename() {
        return this.filename;
    }
    public void setFileName(MainActivity.FILES f) {
        filename = f;
    }
    public ItemsAdapter getIa() {
        return ia;
    }
    public void setIa(ItemsAdapter ia) {
        this.ia = ia;
    }
}
