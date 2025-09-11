package com.xypay.xypay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for safe money calculations.
 */
@Component
public class SafeMoneyCalculationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SafeMoneyCalculationUtil.class);
    
    /**
     * Safely add two BigDecimal values.
     *
     * @param a First value
     * @param b Second value
     * @return Sum of a and b, or zero if either is null
     */
    public BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        try {
            if (a == null && b == null) {
                return BigDecimal.ZERO;
            }
            if (a == null) {
                return b;
            }
            if (b == null) {
                return a;
            }
            return a.add(b);
        } catch (Exception e) {
            logger.error("Error in safeAdd: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Safely subtract two BigDecimal values.
     *
     * @param a First value (minuend)
     * @param b Second value (subtrahend)
     * @return Difference of a and b, or zero if either is null
     */
    public BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        try {
            if (a == null && b == null) {
                return BigDecimal.ZERO;
            }
            if (a == null) {
                return b.negate();
            }
            if (b == null) {
                return a;
            }
            return a.subtract(b);
        } catch (Exception e) {
            logger.error("Error in safeSubtract: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Safely multiply two BigDecimal values.
     *
     * @param a First value
     * @param b Second value
     * @return Product of a and b, or zero if either is null
     */
    public BigDecimal safeMultiply(BigDecimal a, BigDecimal b) {
        try {
            if (a == null || b == null) {
                return BigDecimal.ZERO;
            }
            return a.multiply(b);
        } catch (Exception e) {
            logger.error("Error in safeMultiply: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Safely divide two BigDecimal values.
     *
     * @param dividend The dividend
     * @param divisor The divisor
     * @param scale The scale of the result
     * @param roundingMode The rounding mode
     * @return Quotient of dividend and divisor, or zero if either is null or divisor is zero
     */
    public BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor, int scale, RoundingMode roundingMode) {
        try {
            if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return dividend.divide(divisor, scale, roundingMode);
        } catch (Exception e) {
            logger.error("Error in safeDivide: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Handle service errors.
     *
     * @param operation The operation that failed
     * @param exception The exception that occurred
     */
    public void handleServiceError(String operation, Exception exception) {
        logger.error("Service error in operation {}: {}", operation, exception.getMessage());
        // In a real implementation, you might want to:
        // 1. Send alerts to monitoring systems
        // 2. Record the error in a database
        // 3. Trigger circuit breaker patterns
        // 4. Notify administrators
    }
}