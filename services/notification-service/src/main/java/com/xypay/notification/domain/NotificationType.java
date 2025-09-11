package com.xypay.notification.domain;

public enum NotificationType {
    // E-commerce and system events
    NEW_ORDER("new_order", "New Order"),
    ORDER_STATUS_UPDATE("order_status_update", "Order Status Update"),
    ORDER_CANCELLED("order_cancelled", "Order Cancelled"),
    ORDER_DELIVERED("order_delivered", "Order Delivered"),
    PAYMENT_SUCCESS("payment_success", "Payment Successful"),
    PAYMENT_FAILED("payment_failed", "Payment Failed"),
    PASSWORD_RESET("password_reset", "Password Reset"),
    EMAIL_VERIFICATION("email_verification", "Email Verification"),
    PROMOTION("promotion", "Promotion"),
    FLASH_SALE("flash_sale", "Flash Sale"),
    NEW_MESSAGE("new_message", "New Message"),
    SYSTEM_ALERT("system_alert", "System Alert"),
    WISHLIST_UPDATE("wishlist_update", "Wishlist Update"),
    REVIEW_REMINDER("review_reminder", "Review Reminder"),
    STOCK_ALERT("stock_alert", "Stock Alert"),
    PRICE_DROP("price_drop", "Price Drop"),
    SHIPPING_UPDATE("shipping_update", "Shipping Update"),
    REFUND_PROCESSED("refund_processed", "Refund Processed"),
    ACCOUNT_UPDATE("account_update", "Account Update"),
    SECURITY_ALERT("security_alert", "Security Alert"),
    ESCALATION("escalation", "Escalation"),
    OTHER("other", "Other"),
    SMS("sms", "Sms"),
    EMAIL("email", "Email"),
    PUSH("push", "Push"),
    // Banking events
    BANK_TRANSACTION("bank_transaction", "Bank Transaction"),
    BANK_TRANSFER("bank_transfer", "Bank Transfer"),
    BILL_PAYMENT("bill_payment", "Bill Payment"),
    WALLET_CREDIT("wallet_credit", "Wallet Credited"),
    WALLET_DEBIT("wallet_debit", "Wallet Debited"),
    // Spend and Save specific events
    SPEND_AND_SAVE_ACTIVATION("spend_and_save_activation", "Spend and Save Activation"),
    SPEND_AND_SAVE_DEACTIVATION("spend_and_save_deactivation", "Spend and Save Deactivation"),
    AUTOMATIC_SAVE("automatic_save", "Automatic Save"),
    SAVINGS_MILESTONE("savings_milestone", "Savings Milestone"),
    INTEREST_CREDITED("interest_credited", "Interest Credited"),
    SAVINGS_WITHDRAWAL("savings_withdrawal", "Savings Withdrawal"),
    WEEKLY_SAVINGS_SUMMARY("weekly_savings_summary", "Weekly Savings Summary"),
    SAVINGS_GOAL_ACHIEVED("savings_goal_achieved", "Savings Goal Achieved"),
    // Target Saving specific events
    TARGET_SAVING_CREATED("target_saving_created", "Target Saving Created"),
    TARGET_SAVING_UPDATED("target_saving_updated", "Target Saving Updated"),
    TARGET_SAVING_COMPLETED("target_saving_completed", "Target Saving Completed"),
    TARGET_SAVING_DEPOSIT("target_saving_deposit", "Target Saving Deposit"),
    TARGET_SAVING_MILESTONE("target_saving_milestone", "Target Saving Milestone"),
    TARGET_SAVING_OVERDUE("target_saving_overdue", "Target Saving Overdue"),
    TARGET_SAVING_REMINDER("target_saving_reminder", "Target Saving Reminder"),
    TARGET_SAVING_WITHDRAWAL("target_saving_withdrawal", "Target Saving Withdrawal"),
    // Fixed Savings specific events
    FIXED_SAVINGS_CREATED("fixed_savings_created", "Fixed Savings Created"),
    FIXED_SAVINGS_ACTIVATED("fixed_savings_activated", "Fixed Savings Activated"),
    FIXED_SAVINGS_MATURED("fixed_savings_matured", "Fixed Savings Matured"),
    FIXED_SAVINGS_PAID_OUT("fixed_savings_paid_out", "Fixed Savings Paid Out"),
    FIXED_SAVINGS_INTEREST_CREDITED("fixed_savings_interest_credited", "Fixed Savings Interest Credited"),
    FIXED_SAVINGS_AUTO_RENEWAL("fixed_savings_auto_renewal", "Fixed Savings Auto Renewal"),
    FIXED_SAVINGS_MATURITY_REMINDER("fixed_savings_maturity_reminder", "Fixed Savings Maturity Reminder"),
    FIXED_SAVINGS_EARLY_WITHDRAWAL("fixed_savings_early_withdrawal", "Fixed Savings Early Withdrawal");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
