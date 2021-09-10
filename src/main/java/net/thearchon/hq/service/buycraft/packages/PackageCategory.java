package net.thearchon.hq.service.buycraft.packages;

import java.util.ArrayList;
import java.util.List;

public class PackageCategory {

    private final int id;
    private final String name;
    private final List<PackageModel> packages;

    PackageCategory(int id, String name) {
        this.packages = new ArrayList<>(1);
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PackageModel> getPackages() {
        return packages;
    }

    protected void addPackage(PackageModel pkg) {
        packages.add(pkg);
    }
}
