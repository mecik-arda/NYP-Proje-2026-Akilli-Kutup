package com.akillikutup.core;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Kullanici {

    /** TOTP 2FA için secret key (Base64). Boş/null ise 2FA kapalıdır. */
    @Column(name = "totp_secret_key", length = 100)
    private String totpSecretKey;

    /** 2FA etkin mi? */
    @Column(name = "iki_fa_etkin")
    private boolean ikiFAEtkin = false;

    /** JPA için zorunlu no-arg constructor (protected). */
    protected Admin() {
        super();
    }

    public Admin(String isim, String tcNo, String sifre) {
        super(isim, tcNo, "ADMIN", sifre);
    }

    public String getTotpSecretKey() { return totpSecretKey; }
    public void setTotpSecretKey(String totpSecretKey) { this.totpSecretKey = totpSecretKey; }

    public boolean isIkiFAEtkin() { return ikiFAEtkin; }
    public void setIkiFAEtkin(boolean ikiFAEtkin) { this.ikiFAEtkin = ikiFAEtkin; }

    @Override
    public void goreviniYap() {
        System.out.println("Admin Yonetim Paneli Aktif. Envanter guncellenebilir.");
    }

    public void envanterEkle(Materyal m) {
        System.out.println("ENVANTER GUNCELLEME: '" + m.getBaslik() + "' sisteme kaydedildi.");
    }
}