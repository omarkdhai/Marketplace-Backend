package org.marketplace.notification.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationPreferences {

    // General enable flags
    private boolean emailEnabled = true;
    private boolean pushEnabled = true;

    // Specific notification type flags (Email)
    private boolean emailOrderStatus = true;
    private boolean emailLowStock = true;
    private boolean emailPromotions = true;

    // Specific notification type flags (Push)
    private boolean pushOrderStatus = true;
    private boolean pushLowStock = true;
    private boolean pushPromotions = true;

    // Attribute Keys (used for Keycloak User Attributes)
    public static final String ATTR_EMAIL_ENABLED = "pref_email_enabled";
    public static final String ATTR_PUSH_ENABLED = "pref_push_enabled";
    public static final String ATTR_EMAIL_ORDER_STATUS = "pref_email_order_status";
    public static final String ATTR_EMAIL_LOW_STOCK = "pref_email_low_stock";
    public static final String ATTR_EMAIL_PROMOTIONS = "pref_email_promotions";
    public static final String ATTR_PUSH_ORDER_STATUS = "pref_push_order_status";
    public static final String ATTR_PUSH_LOW_STOCK = "pref_push_low_stock";
    public static final String ATTR_PUSH_PROMOTIONS = "pref_push_promotions";


    // Getters and Setters
    // Add standard getters and setters for all boolean fields
    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public boolean isPushEnabled() { return pushEnabled; }
    public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    public boolean isEmailOrderStatus() { return emailOrderStatus; }
    public void setEmailOrderStatus(boolean emailOrderStatus) { this.emailOrderStatus = emailOrderStatus; }
    public boolean isEmailLowStock() { return emailLowStock; }
    public void setEmailLowStock(boolean emailLowStock) { this.emailLowStock = emailLowStock; }
    public boolean isEmailPromotions() { return emailPromotions; }
    public void setEmailPromotions(boolean emailPromotions) { this.emailPromotions = emailPromotions; }
    public boolean isPushOrderStatus() { return pushOrderStatus; }
    public void setPushOrderStatus(boolean pushOrderStatus) { this.pushOrderStatus = pushOrderStatus; }
    public boolean isPushLowStock() { return pushLowStock; }
    public void setPushLowStock(boolean pushLowStock) { this.pushLowStock = pushLowStock; }
    public boolean isPushPromotions() { return pushPromotions; }
    public void setPushPromotions(boolean pushPromotions) { this.pushPromotions = pushPromotions; }


    // Utility Methods for Keycloak Attribute
    //Creates NotificationPreferences from Keycloak user attributes.
    public static NotificationPreferences fromAttributes(Map<String, List<String>> attributes) {
        NotificationPreferences prefs = new NotificationPreferences();
        if (attributes == null) {
            return prefs;
        }

        prefs.setEmailEnabled(getBooleanAttribute(attributes, ATTR_EMAIL_ENABLED, true));
        prefs.setPushEnabled(getBooleanAttribute(attributes, ATTR_PUSH_ENABLED, true));
        prefs.setEmailOrderStatus(getBooleanAttribute(attributes, ATTR_EMAIL_ORDER_STATUS, true));
        prefs.setEmailLowStock(getBooleanAttribute(attributes, ATTR_EMAIL_LOW_STOCK, true));
        prefs.setEmailPromotions(getBooleanAttribute(attributes, ATTR_EMAIL_PROMOTIONS, true));
        prefs.setPushOrderStatus(getBooleanAttribute(attributes, ATTR_PUSH_ORDER_STATUS, true));
        prefs.setPushLowStock(getBooleanAttribute(attributes, ATTR_PUSH_LOW_STOCK, true));
        prefs.setPushPromotions(getBooleanAttribute(attributes, ATTR_PUSH_PROMOTIONS, true));

        return prefs;
    }


    // Converts NotificationPreferences to a map suitable for Keycloak user attributes.
    public Map<String, List<String>> toAttributes() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ATTR_EMAIL_ENABLED, Collections.singletonList(String.valueOf(this.emailEnabled)));
        attributes.put(ATTR_PUSH_ENABLED, Collections.singletonList(String.valueOf(this.pushEnabled)));
        attributes.put(ATTR_EMAIL_ORDER_STATUS, Collections.singletonList(String.valueOf(this.emailOrderStatus)));
        attributes.put(ATTR_EMAIL_LOW_STOCK, Collections.singletonList(String.valueOf(this.emailLowStock)));
        attributes.put(ATTR_EMAIL_PROMOTIONS, Collections.singletonList(String.valueOf(this.emailPromotions)));
        attributes.put(ATTR_PUSH_ORDER_STATUS, Collections.singletonList(String.valueOf(this.pushOrderStatus)));
        attributes.put(ATTR_PUSH_LOW_STOCK, Collections.singletonList(String.valueOf(this.pushLowStock)));
        attributes.put(ATTR_PUSH_PROMOTIONS, Collections.singletonList(String.valueOf(this.pushPromotions)));
        return attributes;
    }

    private static boolean getBooleanAttribute(Map<String, List<String>> attributes, String key, boolean defaultValue) {
        List<String> values = attributes.get(key);
        if (values != null && !values.isEmpty()) {
            return "true".equalsIgnoreCase(values.get(0));
        }
        return defaultValue;
    }
}
