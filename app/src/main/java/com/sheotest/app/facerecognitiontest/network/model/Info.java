package com.sheotest.app.facerecognitiontest.network.model;

/**
 * Created by sheo on 2019-11-26.
 */
public class Info {
    Integer faceCount;
    Size size;

    public Integer getFacecount() {
        return faceCount;
    }

    public Size getSize() {
        return size;
    }

    public class Size {
        Integer width;
        Integer height;

        public Integer getHeight() {
            return height;
        }

        public Integer getWidth() {
            return width;
        }
    }
}
