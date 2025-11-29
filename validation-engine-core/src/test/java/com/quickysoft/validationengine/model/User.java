package com.quickysoft.validationengine.model;

import java.math.BigDecimal;
import java.util.Objects;

public class User {
    private String loginCode;
    private String userId;
    private BigDecimal limit;
    private UserStatus status;
    private String mailId;

    public User() {
    }

    public User(String loginCode, String userId, BigDecimal limit, UserStatus status, String mailId) {
        this.loginCode = loginCode;
        this.userId = userId;
        this.limit = limit;
        this.status = status;
        this.mailId = mailId;
    }

    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(loginCode, user.loginCode) &&
                Objects.equals(userId, user.userId) &&
                Objects.equals(limit, user.limit) &&
                status == user.status &&
                Objects.equals(mailId, user.mailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginCode, userId, limit, status, mailId);
    }

    @Override
    public String toString() {
        return "User{" +
                "loginCode='" + loginCode + '\'' +
                ", userId='" + userId + '\'' +
                ", limit=" + limit +
                ", status=" + status +
                ", mailId='" + mailId + '\'' +
                '}';
    }
}
