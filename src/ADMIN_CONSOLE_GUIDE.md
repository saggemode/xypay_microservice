# XY Pay Admin Console Guide

This guide explains how to use the XY Pay Admin Console, which provides FLEXCUBE-like configuration capabilities for the core banking system.

## Table of Contents

1. [Introduction](#introduction)
2. [Initial Setup Process](#initial-setup-process)
3. [Admin Console Overview](#admin-console-overview)
4. [Customer & Account Setup](#customer--account-setup)
5. [Product Definition](#product-definition)
6. [Interest & Charges Setup](#interest--charges-setup)
7. [Transaction Rules](#transaction-rules)
8. [Risk & Compliance](#risk--compliance)
9. [Branch & Entity Setup](#branch--entity-setup)
10. [Reporting & Analytics](#reporting--analytics)
11. [Workflow & Authorization](#workflow--authorization)
12. [Alerts & Notifications](#alerts--notifications)
13. [Integration Management](#integration-management)
14. [Parameter Maintenance](#parameter-maintenance)
15. [Database Query Tools](#database-query-tools)
16. [Scripting Capabilities](#scripting-capabilities)
17. [API Reference](#api-reference)

## Introduction

The XY Pay Admin Console provides a comprehensive configuration management system similar to Oracle FLEXCUBE. It allows bank administrators to configure all aspects of the core banking system without writing custom code.

## Initial Setup Process

When a bank first deploys XY Pay, they must complete an initialization process to configure the system for their specific needs.

### Web-based Setup

1. Access the system at `http://your-server:8080`
2. You will be redirected to the welcome page
3. Click "Begin Setup Process"
4. Complete the initialization form with:
   - Bank Name: Your institution's legal name
   - Bank Code: Unique identifier for your main branch
   - Administrator Account details:
     - First and last name
     - Username and email
     - Secure password (minimum 6 characters)
5. Submit the form to initialize the system

This process creates:
- The main branch record in the database
- The first administrator user with full system access
- Default system configurations
- Initial database schema (if not already created)

### Post-Setup Access

After initialization, administrators can access:
- Admin Console: `http://your-server:8080/admin/ui/dashboard`
- API endpoints as documented in this guide
- Login page: `http://your-server:8080/login`

### Security Recommendations

1. Immediately change the default administrator password after first login
2. Configure additional administrator accounts as needed
3. Set up proper role-based access controls for different user types
4. Review and customize default system parameters

## Admin Console Overview

The admin console is accessible through RESTful APIs and provides the following modules:

- Customer & Account Setup
- Product Definition
- Interest & Charges Setup
- Transaction Rules
- Risk & Compliance
- Branch & Entity Setup
- Reporting & Analytics
- Workflow & Authorization
- Alerts & Notifications
- Integration Management
- Parameter Maintenance
- Database Query Tools
- Scripting Capabilities

## Customer & Account Setup

Manage customer types, KYC levels, and account configurations.

### API Endpoints

- `POST /admin/customer-account/customer-config` - Create customer configuration
- `GET /admin/customer-account/customer-config/{customerType}` - Get customer configuration
- `PUT /admin/customer-account/customer-config/{id}` - Update customer configuration
- `DELETE /admin/customer-account/customer-config/{id}` - Delete customer configuration
- `POST /admin/customer-account/account-config` - Create account configuration
- `GET /admin/customer-account/account-config/{accountType}` - Get account configuration
- `GET /admin/customer-account/account-config` - Get all account configurations
- `PUT /admin/customer-account/account-config/{id}` - Update account configuration
- `DELETE /admin/customer-account/account-config/{id}` - Delete account configuration

### Example Usage

Create a customer configuration:
```bash
curl -X POST http://localhost:8080/admin/customer-account/customer-config \
  -H "Content-Type: application/json" \
  -d '{
    "customerType": "INDIVIDUAL",
    "kycLevel": "STANDARD",
    "requiredDocuments": "[\"ID_PROOF\", \"ADDRESS_PROOF\"]",
    "riskRating": "LOW"
  }'
```

Create an account configuration:
```bash
curl -X POST http://localhost:8080/admin/customer-account/account-config \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": "SAVINGS",
    "accountNumberFormat": "BRANCH-YEAR-SERIAL",
    "accountNumberPrefix": "SAV",
    "minimumBalance": 100.00,
    "currency": "USD",
    "dailyWithdrawalLimit": 1000.00
  }'
```

## Product Definition

Setup deposit and loan products with their specific configurations.

### API Endpoints

- `POST /admin/product-setup/deposit-config` - Create deposit product
- `GET /admin/product-setup/deposit-config/{accountType}` - Get deposit product
- `GET /admin/product-setup/deposit-config` - Get all deposit products
- `PUT /admin/product-setup/deposit-config/{id}` - Update deposit product
- `DELETE /admin/product-setup/deposit-config/{id}` - Delete deposit product
- `POST /admin/product-setup/loan-config` - Create loan product
- `GET /admin/product-setup/loan-config/{productName}` - Get loan product
- `GET /admin/product-setup/loan-config` - Get all loan products
- `PUT /admin/product-setup/loan-config/{id}` - Update loan product
- `DELETE /admin/product-setup/loan-config/{id}` - Delete loan product

### Example Usage

Create a savings account product:
```bash
curl -X POST http://localhost:8080/admin/product-setup/deposit-config \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": "PREMIUM_SAVINGS",
    "minimumBalance": 1000.00,
    "currency": "USD",
    "dailyWithdrawalLimit": 5000.00,
    "mandatoryFields": "[\"NAME\", \"ADDRESS\", \"PHONE\"]"
  }'
```

Create a home loan product:
```bash
curl -X POST http://localhost:8080/admin/product-setup/loan-config \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "HOME_LOAN",
    "interestRateType": "FLOATING",
    "minInterestRate": 2.5,
    "maxInterestRate": 8.0,
    "repaymentFrequency": "MONTHLY",
    "minTermMonths": 12,
    "maxTermMonths": 360,
    "gracePeriodDays": 30,
    "penaltyRate": 2.0
  }'
```

## Interest & Charges Setup

Define interest rates, accrual methods, and charge rules.

### API Endpoints

- `POST /admin/interest-charges/interest-config` - Create interest configuration
- `GET /admin/interest-charges/interest-config/{configName}` - Get interest configuration
- `GET /admin/interest-charges/interest-config` - Get all interest configurations
- `PUT /admin/interest-charges/interest-config/{id}` - Update interest configuration
- `DELETE /admin/interest-charges/interest-config/{id}` - Delete interest configuration
- `POST /admin/interest-charges/charge-config` - Create charge configuration
- `GET /admin/interest-charges/charge-config/{chargeName}` - Get charge configuration
- `GET /admin/interest-charges/charge-config` - Get all charge configurations
- `GET /admin/interest-charges/charge-config/type/{chargeType}` - Get charge configurations by type
- `PUT /admin/interest-charges/charge-config/{id}` - Update charge configuration
- `DELETE /admin/interest-charges/charge-config/{id}` - Delete charge configuration

### Example Usage

Create an interest configuration:
```bash
curl -X POST http://localhost:8080/admin/interest-charges/interest-config \
  -H "Content-Type: application/json" \
  -d '{
    "configurationName": "PREMIUM_SAVINGS_INTEREST",
    "calculationMethod": "COMPOUND",
    "compoundingFrequency": "MONTHLY",
    "standardRate": 3.0,
    "preferentialRateCustomerTypes": "[\"VIP\", \"STAFF\"]",
    "preferentialRate": 3.5
  }'
```

Create a charge configuration:
```bash
curl -X POST http://localhost:8080/admin/interest-charges/charge-config \
  -H "Content-Type: application/json" \
  -d '{
    "chargeName": "ATM_WITHDRAWAL_FEE",
    "chargeType": "WITHDRAWAL",
    "amount": 2.00,
    "percentage": 0.5,
    "minimumAmount": 1.00,
    "maximumAmount": 10.00,
    "currency": "USD"
  }'
```

## Transaction Rules

Configure limits, approval hierarchies, and transaction controls.

### API Endpoints

- `POST /admin/interest-charges/rule-config` - Create rule configuration
- `GET /admin/interest-charges/rule-config/{ruleName}` - Get rule configuration
- `GET /admin/interest-charges/rule-config` - Get all rule configurations
- `GET /admin/interest-charges/rule-config/type/{ruleType}` - Get rule configurations by type
- `PUT /admin/interest-charges/rule-config/{id}` - Update rule configuration
- `DELETE /admin/interest-charges/rule-config/{id}` - Delete rule configuration

### Example Usage

Create a transaction limit rule:
```bash
curl -X POST http://localhost:8080/admin/interest-charges/rule-config \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "DAILY_CASH_WITHDRAWAL_LIMIT",
    "ruleType": "LIMIT",
    "dailyLimit": 2000.00
  }'
```

Create an approval rule:
```bash
curl -X POST http://localhost:8080/admin/interest-charges/rule-config \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "HIGH_VALUE_TRANSACTION_APPROVAL",
    "ruleType": "APPROVAL",
    "approvalThreshold": 50000.00,
    "approvalLevels": 2
  }'
```

## Risk & Compliance

Set up AML/KYC rules, sanctions screening, and fraud detection.

### API Endpoints

- `POST /admin/risk-compliance/compliance-config` - Create compliance configuration
- `GET /admin/risk-compliance/compliance-config/{configName}` - Get compliance configuration
- `GET /admin/risk-compliance/compliance-config` - Get all compliance configurations
- `GET /admin/risk-compliance/compliance-config/type/{configType}` - Get compliance configurations by type
- `PUT /admin/risk-compliance/compliance-config/{id}` - Update compliance configuration
- `DELETE /admin/risk-compliance/compliance-config/{id}` - Delete compliance configuration

### Example Usage

Create an AML configuration:
```bash
curl -X POST http://localhost:8080/admin/risk-compliance/compliance-config \
  -H "Content-Type: application/json" \
  -d '{
    "configurationName": "STANDARD_AML",
    "configType": "AML",
    "amlThresholdAmount": 10000.00,
    "sanctionListUrl": "https://ofac.treasury.gov/sanctions-list",
    "fraudDetectionRules": "[{\"rule\": \"FAILED_LOGIN_ATTEMPTS\", \"limit\": 5}]",
    "blacklistAccounts": "[\"BLACKLISTED_ACCOUNT_001\"]",
    "alertRecipients": "[\"aml_officer@bank.com\"]"
  }'
```

## Branch & Entity Setup

Configure multi-branch structure and regional settings.

### API Endpoints

- `POST /admin/risk-compliance/branch-config` - Create branch configuration
- `GET /admin/risk-compliance/branch-config/{branchCode}` - Get branch configuration
- `GET /admin/risk-compliance/branch-config` - Get all branch configurations
- `GET /admin/risk-compliance/branch-config/type/{entityType}` - Get branch configurations by type
- `PUT /admin/risk-compliance/branch-config/{id}` - Update branch configuration
- `DELETE /admin/risk-compliance/branch-config/{id}` - Delete branch configuration

### Example Usage

Create a branch configuration:
```bash
curl -X POST http://localhost:8080/admin/risk-compliance/branch-config \
  -H "Content-Type: application/json" \
  -d '{
    "branchCode": "NYC001",
    "branchName": "New York Main Branch",
    "entityType": "BRANCH",
    "countryCode": "US",
    "currency": "USD",
    "glAccountPrefix": "NYC",
    "regionalHolidays": "[\"2023-01-01\", \"2023-12-25\"]",
    "workingHours": "{\"MONDAY\": \"09:00-17:00\"}"
  }'
```

## Reporting & Analytics

Configure reports, dashboards, and analytics.

### API Endpoints

- `POST /admin/reporting-workflow/reporting-config` - Create reporting configuration
- `GET /admin/reporting-workflow/reporting-config/{reportName}` - Get reporting configuration
- `GET /admin/reporting-workflow/reporting-config` - Get all reporting configurations
- `GET /admin/reporting-workflow/reporting-config/type/{reportType}` - Get reporting configurations by type
- `PUT /admin/reporting-workflow/reporting-config/{id}` - Update reporting configuration
- `DELETE /admin/reporting-workflow/reporting-config/{id}` - Delete reporting configuration

### Example Usage

Create a regulatory report configuration:
```bash
curl -X POST http://localhost:8080/admin/reporting-workflow/reporting-config \
  -H "Content-Type: application/json" \
  -d '{
    "reportName": "DAILY_REGULATORY_REPORT",
    "reportType": "REGULATORY",
    "frequency": "DAILY",
    "format": "PDF",
    "recipients": "[\"regulatory@bank.com\"]",
    "scheduledTime": "06:00",
    "parameters": "{\"report_type\": \"LOAN_DISBURSEMENT\"}",
    "dashboardWidgets": "[{\"widget\": \"LOAN_DISBURSEMENT_CHART\"}]"
  }'
```

## Workflow & Authorization

Configure workflows, approval hierarchies, and field-level permissions.

### API Endpoints

- `POST /admin/reporting-workflow/workflow-config` - Create workflow configuration
- `GET /admin/reporting-workflow/workflow-config/{workflowName}` - Get workflow configuration
- `GET /admin/reporting-workflow/workflow-config` - Get all workflow configurations
- `GET /admin/reporting-workflow/workflow-config/process-type/{processType}` - Get workflow configurations by process type
- `PUT /admin/reporting-workflow/workflow-config/{id}` - Update workflow configuration
- `DELETE /admin/reporting-workflow/workflow-config/{id}` - Delete workflow configuration

### Example Usage

Create a workflow configuration:
```bash
curl -X POST http://localhost:8080/admin/reporting-workflow/workflow-config \
  -H "Content-Type: application/json" \
  -d '{
    "workflowName": "LOAN_APPROVAL_WORKFLOW",
    "processType": "LOAN_APPROVAL",
    "steps": "[{\"step\": \"INITIATION\", \"role\": \"LOAN_OFFICER\"}, {\"step\": \"APPROVAL\", \"role\": \"MANAGER\"}]",
    "fieldAccessControl": "{\"LOAN_OFFICER\": [\"CUSTOMER_INFO\"], \"MANAGER\": [\"ALL_FIELDS\"]}",
    "rolePermissions": "{\"LOAN_OFFICER\": [\"CREATE\"], \"MANAGER\": [\"APPROVE\", \"REJECT\"]}"
  }'
```

## Alerts & Notifications

Set up SMS, email, and in-app notifications.

### API Endpoints

- `POST /admin/alerts-integration/alert-config` - Create alert configuration
- `GET /admin/alerts-integration/alert-config/{alertName}` - Get alert configuration
- `GET /admin/alerts-integration/alert-config` - Get all alert configurations
- `GET /admin/alerts-integration/alert-config/type/{alertType}` - Get alert configurations by type
- `PUT /admin/alerts-integration/alert-config/{id}` - Update alert configuration
- `DELETE /admin/alerts-integration/alert-config/{id}` - Delete alert configuration

### Example Usage

Create an alert configuration:
```bash
curl -X POST http://localhost:8080/admin/alerts-integration/alert-config \
  -H "Content-Type: application/json" \
  -d '{
    "alertName": "HIGH_VALUE_TRANSACTION_ALERT",
    "alertType": "TRANSACTION",
    "notificationChannels": "[\"SMS\", \"EMAIL\"]",
    "templates": "{\"SMS\": \"Transaction of {amount} completed\", \"EMAIL\": \"Dear customer, a transaction was completed\"}",
    "triggerConditions": "{\"amount\": {\"operator\": \">\", \"value\": 500}}",
    "recipients": "[\"CUSTOMER\"]"
  }'
```

## Integration Management

Configure API connections and message formats.

### API Endpoints

- `POST /admin/alerts-integration/integration-config` - Create integration configuration
- `GET /admin/alerts-integration/integration-config/{integrationName}` - Get integration configuration
- `GET /admin/alerts-integration/integration-config` - Get all integration configurations
- `GET /admin/alerts-integration/integration-config/type/{integrationType}` - Get integration configurations by type
- `PUT /admin/alerts-integration/integration-config/{id}` - Update integration configuration
- `DELETE /admin/alerts-integration/integration-config/{id}` - Delete integration configuration

### Example Usage

Create an integration configuration:
```bash
curl -X POST http://localhost:8080/admin/alerts-integration/integration-config \
  -H "Content-Type: application/json" \
  -d '{
    "integrationName": "SWIFT_INTEGRATION",
    "integrationType": "PAYMENT_SWITCH",
    "endpointUrl": "https://swift.bank.com/api",
    "authenticationMethod": "CERTIFICATE",
    "messageFormat": "ISO20022",
    "connectionSettings": "{\"timeout\": 30, \"retries\": 3}",
    "mappingRules": "{\"source_field\": \"swift_field\"}"
  }'
```

## Parameter Maintenance

Direct maintenance of system parameters, similar to FLEXCUBE's core parameter screens.

### API Endpoints

- `GET /admin/parameters` - Get all parameters
- `GET /admin/parameters/{paramName}` - Get a specific parameter
- `POST /admin/parameters` - Create/update a parameter
- `PUT /admin/parameters/{paramName}` - Update a parameter
- `DELETE /admin/parameters/{paramName}` - Delete a parameter

### Example Usage

Get all parameters:
```bash
curl -X GET http://localhost:8080/admin/parameters
```

Set a parameter:
```bash
curl -X POST http://localhost:8080/admin/parameters \
  -H "Content-Type: application/json" \
  -d '{
    "name": "BANK_NAME",
    "value": "Global Bank"
  }'
```

## Database Query Tools

Direct database query capabilities, similar to FLEXCUBE's database tools.

### API Endpoints

- `POST /admin/database/query` - Execute a database query
- `POST /admin/database/update` - Execute a database update

### Example Usage

Execute a query:
```bash
curl -X POST http://localhost:8080/admin/database/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "SELECT * FROM customers WHERE kyc_status = \"APPROVED\""
  }'
```

Execute an update:
```bash
curl -X POST http://localhost:8080/admin/database/update \
  -H "Content-Type: application/json" \
  -d '{
    "query": "UPDATE accounts SET balance = balance + 100 WHERE account_number = \"ACC001\""
  }'
```

## Scripting Capabilities

Execute custom scripts for advanced logic, similar to FLEXCUBE's scripting capabilities.

### API Endpoints

- `POST /admin/scripting/execute` - Execute a script
- `POST /admin/scripting/validate` - Validate a script

### Example Usage

Execute a script:
```bash
curl -X POST http://localhost:8080/admin/scripting/execute \
  -H "Content-Type: application/json" \
  -d '{
    "script": "function calculateInterest(principal, rate, time) { return principal * rate * time / 100; } calculateInterest(1000, 5, 2);",
    "context": {
      "principal": 1000,
      "rate": 5,
      "time": 2
    }
  }'
```

Validate a script:
```bash
curl -X POST http://localhost:8080/admin/scripting/validate \
  -H "Content-Type: application/json" \
  -d '{
    "script": "function calculateInterest(principal, rate, time) { return principal * rate * time / 100; }"
  }'
```

## API Reference

All admin console APIs follow the same pattern:

### Create Configuration
```
POST /admin/{module}/{config-type}
Content-Type: application/json

{...configuration data...}
```

### Get Configuration
```
GET /admin/{module}/{config-type}/{identifier}
```

### Get All Configurations of Type
```
GET /admin/{module}/{config-type}
```

### Update Configuration
```
PUT /admin/{module}/{config-type}/{identifier}
Content-Type: application/json

{...updated configuration data...}
```

### Delete Configuration
```
DELETE /admin/{module}/{config-type}/{identifier}
```

## Dashboard Access

Access the admin console dashboard:
```
GET /admin/console/dashboard
```

Get system information:
```
GET /admin/console/system-info
```

Get admin tools:
```
GET /admin/console/tools
```

## Best Practices

1. **Test Before Deployment**: Always test configurations in a sandbox environment before applying to production.

2. **Version Control**: Maintain version control of your configurations, especially for compliance-related settings.

3. **Regular Audits**: Regularly audit your configurations to ensure they meet current regulatory requirements.

4. **Backup Configurations**: Regularly backup your configuration data to ensure quick recovery in case of issues.

5. **Documentation**: Maintain documentation of all configurations for compliance and operational purposes.

6. **Gradual Rollout**: For major configuration changes, consider rolling them out gradually to minimize risk.

## Conclusion

The XY Pay Admin Console provides banks with the flexibility to configure their core banking system similar to Oracle FLEXCUBE, allowing them to set up customer types, account requirements, product configurations, interest and charge structures, transaction rules, compliance settings, branch configurations, reporting schedules, workflow personalization, alert notifications, and integration points entirely through configuration rather than code changes.