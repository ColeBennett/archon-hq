package net.thearchon.hq.payment;

public class Payment {

    private final String transactionId;
    private final PaymentStatus status;
    private final double price;
    private final String currency;
    private final String packageName;
    private final String date;
    private final String time;
    private final String email;
    private final String ipAddress;
    private final String serverName;
    private final String uuid;
    private final String username;

    public Payment(String transactionId, PaymentStatus status, double price,
            String currency, String packageName, String date, String time,
            String email, String ipAddress, String serverName,
            String uuid, String username) {
        this.transactionId = transactionId;
        this.status = status;
        this.price = price;
        this.currency = currency;
        this.packageName = packageName;
        this.date = date;
        this.time = time;
        this.email = email;
        this.ipAddress = ipAddress;
        this.serverName = serverName;
        this.uuid = uuid;
        this.username = username;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getEmail() {
        return email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getServerName() {
        return serverName;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "Payment(ign: " + username + ", uuid: " + uuid
                + ", server: " + serverName + ", txid: " + transactionId
                + ", status: " + status + ", paid: " + price + ", currency: "
                + currency + ", package: " + packageName + ", date: " + date
                + " " + time + ", email: " + email + ", ip: " + ipAddress + ')';
    }
}
