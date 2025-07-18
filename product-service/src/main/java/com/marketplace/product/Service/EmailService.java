package com.marketplace.product.Service;

import com.marketplace.product.Entity.CartProduct;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Entity.Product;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "config.email.background.url")
    String backgroundURL;

    @ConfigProperty(name = "config.email.logo.url")
    String logoURL;

    public void sendOrderConfirmationEmail(ProceedOrder order) {
        System.out.println("===> [EmailService] Preparing confirmation email to: " + order.email);
        try {
            InputStream logoStream = getClass().getResourceAsStream("/marketplacelogo1.png");
            if (logoStream == null) {
                throw new IOException("Could not find marketplacelogo1.png in resources.");
            }
            byte[] logoBytes = logoStream.readAllBytes();
            String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);

            String emailBody = buildOrderConfirmationHtml(order, logoBase64);

            mailer.send(
                    Mail.withHtml(
                            order.email,
                            "Your Marketify Order #" + order.id.toString() + " is Confirmed!",
                            emailBody
                    )
            );
            System.out.println("✅ [EmailService] Confirmation email sent successfully.");
        } catch (Exception e) {
            System.err.println("FAILED to send confirmation email for order " + order.id);
            e.printStackTrace();
        }
    }

    public void sendOrderShippedEmail(ProceedOrder order) {
        System.out.println("===> [EmailService] Preparing shipping notification email to: " + order.email);
        try {
            InputStream logoStream = getClass().getResourceAsStream("/marketplacelogo1.png");
            if (logoStream == null) throw new IOException("Could not find logo.png");
            byte[] logoBytes = logoStream.readAllBytes();
            String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);

            String emailBody = buildOrderShippedHtml(order, logoBase64);

            mailer.send(
                    Mail.withHtml(
                            order.email,
                            "Your Marketplace Order #" + order.id.toString() + " Has Shipped!",
                            emailBody
                    )
            );
            System.out.println("✅ [EmailService] Shipping notification sent successfully.");
        } catch (Exception e) {
            System.err.println("FAILED to send shipping notification for order " + order.id);
            e.printStackTrace();
        }
    }

    public String buildOrderConfirmationHtml(ProceedOrder order, String logoBase64) {
        StringBuilder sb = new StringBuilder();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String formattedOrderDate = order.getCreatedAt().format(dateFormatter);

        sb.append("<!DOCTYPE html><html><head><style>")
                .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f7f7f7; margin: 0; padding: 20px; }")
                .append(".wrapper { max-width: 700px; margin: auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; background-image: url(").append(backgroundURL).append("); background-repeat: no-repeat; background-position: top center; background-size: 100% auto; }")
                .append(".background-blob { position: absolute; top: 0; left: 0; width: 100%; height: 1000px; background-image: url(").append(backgroundURL).append("); background-repeat: no-repeat; background-position: top center; background-size: cover; z-index: 1; }")
                .append(".header, .content { position: relative; z-index: 2; }")
                .append(".header { padding: 20px; text-align: center; }")
                .append(".header .logo-container img { width: 100px; height: auto; display: inline-block; padding: 5px; border-radius: 50%; }")
                .append(".order-no { font-size: 20px; font-weight: bold; color: #444; }")
                .append(".content { padding: 20px 30px 30px 30px; }")
                .append(".title { font-size: 28px; font-weight: bold; color: #444; margin-top: 10px; margin-bottom: 5px; }")
                .append(".subtitle { font-size: 16px; color: #555; margin-bottom: 30px; }")
                .append(".product-row { display: flex; align-items: center; padding: 15px 0; border-bottom: 1px dashed #ddd; }")
                .append(".product-image { width: 60px; height: 60px; border-radius: 6px; margin-right: 15px; }")
                .append(".product-info { flex-grow: 1; font-size: 14px; }")
                .append(".product-info .name { font-weight: 600; color: #222; }")
                .append(".product-price { font-weight: 600; text-align: right; margin-left: 250px }")
                .append(".summary { display: flex; justify-content: flex-end; margin-top: 20px; }")
                .append(".summary table { width: 50%; max-width: 250px; font-size: 14px; }")
                .append(".summary td { padding: 5px 0; }")
                .append(".grand-total { font-weight: bold; border-top: 2px solid #333; }")
                .append(".address-section { display: flex; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }")
                .append(".address { flex: 1; padding: 0 10px; margin-left: 100px; font-size: 14px; color: #555; }")
                .append(".footer { text-align: center; font-size: 12px; color: #444; padding: 20px; }")
                .append("</style></head><body>");

        sb.append("<div class='wrapper'>")

                .append("<div class='header'>")
                .append("<div class='logo-container'>")
                .append("<img src='").append(logoURL).append("' alt='Marketplace Logo'/>")
                .append("</div>")
                .append("</div>")

                .append("<div class='content'>")
                .append("<div class='title'>Yesss! Your Order Is Confirmed</div>")
                .append("<p class='subtitle'>Hi ").append(order.firstName).append(", Thank you for your order. We will send you a confirmation when your order is shipped. Please find below the receipt of your purchase.</p>")
                .append("<div class='order-no'>Order ID: ").append(order.id.toString()).append("</div>")

                .append("<div class='order-details-grid' style='margin-top: 20px;'>");

        for (CartProduct item : order.products) {
            Product product = item.getProduct();

            double pricePerLine;
            if (product.getDiscount() > 0) {
                pricePerLine = product.getEffectivePrice() * item.getQuantity();
            } else {
                pricePerLine = product.getPrice() * item.getQuantity();
            }


            sb.append("<div class='product-row'>")
                    .append("<div><img src='").append(product.getImgUrl()).append("' class='product-image' alt='product'></div>")
                    .append("<div class='product-info'>")
                    .append("<div class='name'>").append(product.getName()).append("</div>")
                    .append("<div class='specs'>Quantity: ").append(item.getQuantity()).append("</div>")
                    .append("</div>")
                    .append("<div class='product-price'>").append(String.format("%.2f TND", pricePerLine)).append("</div>")
                    .append("</div>");
        }

        sb.append("</div>")
                .append("<div class='summary'><table>")
                .append("<tr><td>Total :</td><td style='text-align: right;'>").append(String.format("%.2f TND", order.totalPrice)).append("</td></tr>")
                .append("<tr><td>Shipping Charges :</td><td style='text-align: right;'>0.00 TND</td></tr>")
                .append("<tr class='grand-total'><td>Grand Total :</td><td style-align: right;'>").append(String.format("%.2f TND", order.totalPrice)).append("</td></tr>")
                .append("</table></div>")
                .append("<div class='address-section'>")
                .append("<div class='address'><strong>Shipping Address:</strong><br>")
                .append(order.streetAddress).append("<br>")
                .append(order.city).append(", ").append(order.postalCode)
                .append("</div>")
                .append("<div class='address'><strong>Billing Address:</strong><br>")
                .append("------------------").append("<br>")
                .append("------------------")
                .append("</div>")
                .append("</div>")
                .append("<p style='margin-top: 60px;'>Hope to see you soon,<br><strong>Marketify Team</strong></p>")
                .append("</div>")
                .append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    public String buildOrderShippedHtml(ProceedOrder order, String logoBase64) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html><html><head><style>")
                .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f7f7f7; margin: 0; padding: 20px; }")
                .append(".wrapper { max-width: 700px; margin: auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; background-image: url(").append(backgroundURL).append("); background-repeat: no-repeat; background-position: top center; background-size: 100% auto; }")
                .append(".header, .content { position: relative; z-index: 2; }")
                .append(".header { padding: 20px; text-align: center; }")
                .append(".header .logo-container img { width: 100px; height: auto; display: inline-block; padding: 5px; border-radius: 50%; }")
                .append(".order-no { font-size: 20px; font-weight: bold; color: #444; }")
                .append(".content { padding: 20px 30px 30px 30px; }")
                .append(".title { font-size: 28px; font-weight: bold; color: #444; margin-top: 10px; margin-bottom: 5px; }")
                .append(".subtitle { font-size: 16px; color: #555; margin-bottom: 30px; }")
                .append(".product-row { display: flex; align-items: center; padding: 15px 0; border-bottom: 1px dashed #ddd; }")
                .append(".product-image { width: 60px; height: 60px; border-radius: 6px; margin-right: 15px; }")
                .append(".product-info { flex-grow: 1; font-size: 14px; }")
                .append(".product-info .name { font-weight: 600; color: #222; }")
                .append(".product-price { font-weight: 600; text-align: right; margin-left: 250px }")
                .append(".summary { display: flex; justify-content: flex-end; margin-top: 20px; }")
                .append(".summary table { width: 50%; max-width: 250px; font-size: 14px; }")
                .append(".summary td { padding: 5px 0; }")
                .append(".grand-total { font-weight: bold; border-top: 2px solid #333; }")
                .append(".address-section { display: flex; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }")
                .append(".address { flex: 1; padding: 0 10px; margin-left: 100px; font-size: 14px; color: #555; }")
                .append(".footer { text-align: center; font-size: 12px; color: #444; padding: 20px; }")
                .append(".tracking-box { background-color: #f0f8ff; border: 1px solid #add8e6; padding: 15px; text-align: center; margin: 20px 0; border-radius: 6px; }")
                .append(".tracking-box .label { font-size: 14px; color: #555; }")
                .append(".tracking-box .number { font-size: 18px; font-weight: bold; color: #111; letter-spacing: 1px; margin-top: 5px; }")
                .append("</style></head><body>");

        sb.append("<div class='wrapper'>")
                .append("<div class='background-blob'></div>")
                .append("<div class='header'>")
                .append("<div class='logo-container'><img src='").append(logoURL).append("' alt='Logo'/></div>")
                .append("</div>")
                .append("<div class='content'>")

                .append("<div class='title'>Your Order is On Its Way!</div>")
                .append("<div class='order-no'>Order ID: ").append(order.id.toString()).append("</div>")
                .append("<p class='subtitle'>Hi ").append(order.firstName).append(", good news! Your order has been shipped.</p>")

                .append("<div class='tracking-box'>")
                .append("<div class='label'>You can track your package with this number:</div>")
                .append("<div class='number'>").append(order.getTrackingNumber()).append("</div>")
                .append("</div>")

                .append("<p class='subtitle'>Your will receive your order as soon as possible.</p>")


                .append("<p>Best regards,<br><strong>Marketify Team</strong></p>")
                .append("</div>")
                .append("</div></body></html>");

        return sb.toString();
    }
}
