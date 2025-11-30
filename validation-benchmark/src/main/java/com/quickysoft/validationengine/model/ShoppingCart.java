package com.quickysoft.validationengine.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a shopping cart containing line items and a user.
 */
public class ShoppingCart {
    private List<LineItem> lineItems;
    private User user;
    private BigDecimal cartTotalAmount;

    public ShoppingCart() {
        this.lineItems = new ArrayList<>();
    }

    public ShoppingCart(List<LineItem> lineItems, User user, BigDecimal cartTotalAmount) {
        this.lineItems = lineItems != null ? new ArrayList<>(lineItems) : new ArrayList<>();
        this.user = user;
        this.cartTotalAmount = cartTotalAmount;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems != null ? new ArrayList<>(lineItems) : new ArrayList<>();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getCartTotalAmount() {
        return cartTotalAmount;
    }

    public void setCartTotalAmount(BigDecimal cartTotalAmount) {
        this.cartTotalAmount = cartTotalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(lineItems, that.lineItems) &&
                Objects.equals(user, that.user) &&
                Objects.equals(cartTotalAmount, that.cartTotalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineItems, user, cartTotalAmount);
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "lineItems=" + lineItems +
                ", user=" + user +
                ", cartTotalAmount=" + cartTotalAmount +
                '}';
    }
}

