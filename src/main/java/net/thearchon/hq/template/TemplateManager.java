package net.thearchon.hq.template;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.Client;

import java.io.File;

public class TemplateManager {

    private final File baseDir = new File("template");

    public TemplateManager(Archon archon) {
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
    }

    public boolean update(Client client) {
        File templateDir = new File(baseDir, client.getType().name());
        if (!templateDir.exists()) {
            templateDir.mkdir();
        }

//        File[] files = templateDir.listFiles();
//        if (files.length == 0) return false;
//
//        for (File file : files) {
//            if (!file.isDirectory()) {
//                client.send(new FilePacket(file, new BufferedPacket(1).writeString("")));
//            }
//        }
        return true;
    }
}
