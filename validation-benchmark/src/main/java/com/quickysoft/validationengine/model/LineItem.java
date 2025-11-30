package com.quickysoft.validationengine.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a line item in a shopping cart.
 */
public class LineItem {
    private String id;
    private Product product;
    private int quantity;
    private BigDecimal appliedDiscount;

    public LineItem() {
    }

    public LineItem(String id, Product product, int quantity, BigDecimal appliedDiscount) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.appliedDiscount = appliedDiscount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAppliedDiscount() {
        return appliedDiscount;
    }

    public void setAppliedDiscount(BigDecimal appliedDiscount) {
        this.appliedDiscount = appliedDiscount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineItem lineItem = (LineItem) o;
        return quantity == lineItem.quantity &&
                Objects.equals(id, lineItem.id) &&
                Objects.equals(product, lineItem.product) &&
                Objects.equals(appliedDiscount, lineItem.appliedDiscount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product, quantity, appliedDiscount);
    }

    @Override
    public String toString() {
        return "LineItem{" +
                "id='" + id + '\'' +
                ", product=" + product +
                ", quantity=" + quantity +
                ", appliedDiscount=" + appliedDiscount +
                '}';
    }
}

