package net.thearchon.hq.service.buycraft.packages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PackageManager {

    private final List<PackageCategory> categories;
    private final List<PackageModel> packages;

    public PackageManager() {
        categories = Collections.synchronizedList(new ArrayList<>());
        packages = Collections.synchronizedList(new ArrayList<>());
    }

    public List<PackageCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public List<PackageModel> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public void addCategory(int categoryId, String name) {
        categories.add(new PackageCategory(categoryId, name));
    }

    public void addPackage(int categoryId, int id, String name, double price) {
        PackageCategory category = getPackageCategory(categoryId);
        if (category == null) {
            System.out.println("Category not found for package: " + name);
            return;
        }
        PackageModel pkg = new PackageModel(category, id, name, price, packages.size() + 1);
        category.addPackage(pkg);
        packages.add(pkg);
    }

    public void cleanCategories() {
        synchronized (categories) {
            Iterator<PackageCategory> itr = categories.iterator();
            while (itr.hasNext()) {
                if (itr.next().getPackages().isEmpty()) {
                    itr.remove();
                }
            }
        }
    }
    
    public boolean hasPackages() {
        return !categories.isEmpty();
    }

    public PackageCategory getPackageCategory(int categoryId) {
        for (PackageCategory c : categories) {
            if (c.getId() == categoryId)
                return c;
        }
        return null;
    }

    public PackageModel getPackageById(int packageId) {
        for (PackageModel pkg : packages) {
            if (pkg.getId() == packageId) {
                return pkg;
            }
        }

        return null;
    }

    public PackageModel getPackageByOrderId(int orderId) {
        for (PackageModel pkg : packages) {
            if (pkg.getOrder() == orderId) {
                return pkg;
            }
        }
        return null;
    }

    public void reset() {
        categories.clear();
        packages.clear();
    }
}
