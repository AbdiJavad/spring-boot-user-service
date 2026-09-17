package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 50, message = "Name muss zwischen 2 und 50 Zeichen lang sein")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Bitte eine gÃ¼ltige E-Mail-Adresse eingeben")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    // ================= UserDetails Implementation =================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Defensive coding: Ø¯Ø± ØµÙˆØ±Øª Ù†Ø§Ù„ Ø¨ÙˆØ¯Ù† Ø±ÙˆÙ„ØŒ Ø¯Ø³ØªØ±Ø³ÛŒ Ù¾ÛŒØ´â€ŒÙØ±Ø¶ ROLE_USER Ù„Ø­Ø§Ø¸ Ù…ÛŒâ€ŒØ´ÙˆØ¯
        return List.of(new SimpleGrantedAuthority(this.role != null ? this.role.name() : "ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
