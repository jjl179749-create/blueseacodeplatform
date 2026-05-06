package org.blueseacode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.blueseacode")
public class BlueSeaCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlueSeaCodeApplication.class, args);
    }
}
