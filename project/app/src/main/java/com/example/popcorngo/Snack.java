package com.example.popcorngo;


public class Snack {
    public int id;
    public String name;
    public double price;
    public int stock;
    public String imageUrl;

    public Snack(int id, String name, double price, int stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }
}
