package com.nowcoder.community;

public class WkTests {
    public static void main(String[] args) {
        String[] cmd = {"/usr/local/bin/wkhtmltoimage", "--quality", "75", "https://www.nowcoder.com", "/home/wxy/Projects/wk-images/1.png"};
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (process.waitFor() == 0) {
                System.out.println("ok.");
            } else {
                System.out.println("error.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
