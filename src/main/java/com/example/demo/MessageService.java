package com.example.demo;

import org.springframework.stereotype.Service;

@Service // Ø§ÛŒÙ† Ú©Ù„ÛŒØ¯ Ø¬Ø§Ø¯ÙˆÛŒÛŒ Ø§Ø³Øª Ú©Ù‡ Ø¨Ù‡ Ø§Ø³Ù¾Ø±ÛŒÙ†Ú¯ Ù…ÛŒâ€ŒÚ¯ÙˆÛŒØ¯: "Ø§ÛŒÙ† Ú©Ù„Ø§Ø³ Ø±Ø§ Ø¨Ø±Ø§ÛŒ Ù…Ù† Ù…Ø¯ÛŒØ±ÛŒØª Ú©Ù†"
public class MessageService {
    public String getMessage() {
        return "Ø³Ù„Ø§Ù…! Ø§ÛŒÙ† Ù¾ÛŒØ§Ù… Ø§Ø² Ø³Ø±ÙˆÛŒØ³ Ù…ÛŒâ€ŒØ¢ÛŒØ¯.";
    }
}
