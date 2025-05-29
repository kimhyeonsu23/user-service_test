package com.budgetmate.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public String sendVerificationCode(String toEmail) {
        log.info("[EmailService] 인증코드 전송 시작 → {}", toEmail);

        String code = String.format("%06d", new Random().nextInt(999999));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[BudgetMate] 이메일 인증 코드");
        message.setText("인증코드: " + code);

        try {
            mailSender.send(message);
            log.info("[EmailService] 이메일 전송 완료 → {}", toEmail);
        } catch (MailException e) {
            log.error("[EmailService] 이메일 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.");
        }

        return code;
    }
}
