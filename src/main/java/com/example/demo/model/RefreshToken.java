package com.example.demo.model;

import jakarta.persistence.*; // اطمینان حاصل کنید که از jakarta.persistence استفاده می‌کنید
import lombok.*;

import java.time.Instant;

@Entity // نشان می‌دهد که این کلاس یک موجودیت پایگاه داده است
@Getter // Lombok برای تولید getters
@Setter // Lombok برای تولید setters
@NoArgsConstructor // Lombok برای سازنده بدون آرگومان (لازم برای JPA)
@AllArgsConstructor // Lombok برای سازنده با تمام آرگومان‌ها
@Builder // Lombok برای الگوی Builder
@Table(name = "refresh_tokens") // نام جدول در پایگاه داده
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // خود توکن refreshToken

    @ManyToOne(fetch = FetchType.LAZY) // رابطه چند به یک با User، بارگذاری تنبل (Lazy Loading)
    @JoinColumn(name = "user_id", nullable = false) // ستون کلید خارجی در جدول refresh_tokens
    private User user; // ارجاع به موجودیت User

    @Column(nullable = false)
    private Instant expiryDate; // تاریخ انقضای توکن
}
