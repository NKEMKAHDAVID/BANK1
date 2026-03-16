package com.thebank;

import com.thebank.enums.CustomerTier;

public class Customer {

    private final int id;
    private String name;
    private CustomerTier tier;

    public Customer(int id, String name, CustomerTier tier) {
        this.id = id;
        this.name = name;
        this.tier = tier;
    }
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CustomerTier getTier() {
        return tier;
    }
    @Override
    public String toString() {
        return String.format("Customer [ID: %d | Name: %s | Tier: %s]", id, name, tier);
    }
}