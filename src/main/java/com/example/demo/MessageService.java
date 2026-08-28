package com.example.demo;

import org.springframework.stereotype.Service;

@Service // این کلید جادویی است که به اسپرینگ می‌گوید: "این کلاس را برای من مدیریت کن"
public class MessageService {
    public String getMessage() {
        return "سلام! این پیام از سرویس می‌آید.";
    }
}
