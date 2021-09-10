package net.thearchon.hq.data;

import java.io.File;

public class DataRepository {

    private final File baseDir;

    public DataRepository() {
        baseDir = new File("data");
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
    }

    public File getBaseDirectory() {
        return baseDir;
    }

    public DataFolder getDirectory(String folderName) {
        File dir = new File(baseDir, folderName);
        dir.mkdir();
        return new DataFolder(dir);
    }

    public static final class DataFolder {
        private final File file;

        DataFolder(File dir) {
            this.file = dir;
        }

//        public <T extends JsonFile> T loadJson(String jsonFile, Class<T> clazz) {
//            if (!jsonFile.endsWith(".json")) {
//                jsonFile += ".json";
//            }
//            File f = new File(file, jsonFile);
//            T data = null;
//            if (!f.exists()) {
//                try {
//                    data = clazz.newInstance();
//                    data.setFile(f);
//                } catch (InstantiationException | IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                data = JsonUtil.load(f, clazz);
//            }
//            if (data == null) {
//                throw new NullPointerException("Failed to set Json File: "
//                        + f.getAbsolutePath());
//            }
//            return data;
//        }

        public DataFolder getDirectory(String folderName) {
            File dir = new File(file, folderName);
            dir.mkdir();
            return new DataFolder(dir);
        }

        public File[] getFiles() {
            return file.listFiles();
        }
    }
}
