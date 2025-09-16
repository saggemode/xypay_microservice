package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.scheduler.WeeklyStatementScheduler.StatementData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for sending emails including statements and notifications
 * Equivalent to Django's EmailMessage functionality
 */
@Service
@Slf4j
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.email.from:noreply@xypay.com}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    /**
     * Send weekly statement email with PDF attachment
     * Equivalent to Django's EmailMessage for weekly statements
     */
    public void sendWeeklyStatement(User user, StatementData statementData, byte[] pdfBytes) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping weekly statement for user: {}", user.getUsername());
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(String.format("Your Weekly Account Statement (%s to %s)", 
                statementData.getDateFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                statementData.getDateTo().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            
            // Prepare template context
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("wallet", statementData.getWallet());
            context.setVariable("transactions", statementData.getTransactions());
            context.setVariable("dateFrom", statementData.getDateFrom());
            context.setVariable("dateTo", statementData.getDateTo());
            context.setVariable("openingBalance", statementData.getOpeningBalance());
            context.setVariable("closingBalance", statementData.getClosingBalance());
            context.setVariable("totalCredits", statementData.getTotalCredits());
            context.setVariable("totalDebits", statementData.getTotalDebits());
            context.setVariable("now", statementData.getGeneratedAt());
            
            // Generate HTML content from template
            String htmlContent = templateEngine.process("bank/statement_pdf", context);
            helper.setText(htmlContent, true);
            
            // Attach PDF
            helper.addAttachment("weekly_statement.pdf", () -> new java.io.ByteArrayInputStream(pdfBytes));
            
            mailSender.send(message);
            log.info("Weekly statement email sent successfully to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Error sending weekly statement email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Send Spend & Save daily summary email
     */
    public void sendSpendSaveDailySummary(User user, Map<String, Object> summaryData) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping daily summary for user: {}", user.getUsername());
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Your Daily Spend & Save Summary");
            
            // Prepare template context
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("summary", summaryData);
            context.setVariable("date", java.time.LocalDate.now());
            
            // Generate HTML content from template
            String htmlContent = templateEngine.process("spend-save/daily_summary", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Daily Spend & Save summary email sent successfully to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Error sending daily summary email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Send Spend & Save weekly summary email
     */
    public void sendSpendSaveWeeklySummary(User user, Map<String, Object> summaryData) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping weekly summary for user: {}", user.getUsername());
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Your Weekly Spend & Save Summary");
            
            // Prepare template context
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("summary", summaryData);
            context.setVariable("weekStart", java.time.LocalDate.now().minusDays(6));
            context.setVariable("weekEnd", java.time.LocalDate.now());
            
            // Generate HTML content from template
            String htmlContent = templateEngine.process("spend-save/weekly_summary", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Weekly Spend & Save summary email sent successfully to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Error sending weekly summary email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Send generic notification email
     */
    public void sendNotification(User user, String subject, String templateName, Map<String, Object> contextData) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping notification for user: {}", user.getUsername());
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            
            // Prepare template context
            Context context = new Context();
            context.setVariable("user", user);
            contextData.forEach(context::setVariable);
            
            // Generate HTML content from template
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Notification email sent successfully to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Error sending notification email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
