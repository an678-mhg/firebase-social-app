package com.example.android_firebase.models;

import java.io.File;

public class Cloudinary {
    File file;
    String upload_presets;

    public Cloudinary(File file, String upload_presets) {
        this.file = file;
        this.upload_presets = upload_presets;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getUpload_presets() {
        return upload_presets;
    }

    public void setUpload_presets(String upload_presets) {
        this.upload_presets = upload_presets;
    }
}
