package net.thearchon.hq.service.buycraft.packages;

public class PackageModel {

    private final PackageCategory category;
    private final int id;
    private final String name;
    private final double price;
    private final int order;

    PackageModel(PackageCategory category, int id,
            String name, double price, int order) {
        this.category = category;
        this.id = id;
        this.name = name;
        this.price = price;
        this.order = order;
    }

    public PackageCategory getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getOrder() {
        return order;
    }
}
