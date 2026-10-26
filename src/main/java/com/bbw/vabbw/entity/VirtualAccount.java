package com.bbw.vabbw.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "virtual_accounts")
public class VirtualAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String virtualAccount;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private boolean active = true;

    public VirtualAccount() {
    }

    public Long getId() {
        return id;
    }

    public String getVirtualAccount() {
        return virtualAccount;
    }

    public void setVirtualAccount(String virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}