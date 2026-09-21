package com.vivek;

import android.content.Context;
import android.util.TypedValue;

public class Utils {

    Context context;

    public Utils(Context globContext) {
        context = globContext;
    }

    public int FixDP(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) i, context.getResources().getDisplayMetrics());
    }

    public int FixDP(float f) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f, context.getResources().getDisplayMetrics());
    }

    public static android.graphics.Bitmap makeBlackTransparent(android.graphics.Bitmap src) {
        if (src == null) return null;
        if (src.hasAlpha()) {
            return src;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        boolean[] visited = new boolean[width * height];
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            queue.add(x);
            queue.add((height - 1) * width + x);
        }
        for (int y = 0; y < height; y++) {
            queue.add(y * width);
            queue.add(y * width + (width - 1));
        }

        while (!queue.isEmpty()) {
            int idx = queue.poll();
            if (visited[idx]) continue;
            visited[idx] = true;
            int c = pixels[idx];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            if (r <= 25 && g <= 25 && b <= 25) {
                pixels[idx] = 0x00000000;
                int x = idx % width;
                int y = idx / width;
                if (x > 0 && !visited[idx - 1]) queue.add(idx - 1);
                if (x < width - 1 && !visited[idx + 1]) queue.add(idx + 1);
                if (y > 0 && !visited[idx - width]) queue.add(idx - width);
                if (y < height - 1 && !visited[idx + width]) queue.add(idx + width);
            }
        }
        android.graphics.Bitmap result = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

}
