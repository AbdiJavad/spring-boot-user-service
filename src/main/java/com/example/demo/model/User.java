package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; // این import را حتماً اضافه کن

@Entity
@Table(name = "app_user") // <-- این خط جدول را به اسم "app_user" تغییر می‌دهد
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    // Constructor, getters, and setters...
    // constructor با ورودی را این‌طوری آپدیت کن:
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User() {

    }

    // ... حتماً برای email هم Getter و Setter بساز (Alt + Insert) ...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
