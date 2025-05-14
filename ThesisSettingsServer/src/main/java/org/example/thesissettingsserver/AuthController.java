package org.example.thesissettingsserver;

import org.example.thesissettingsserver.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utils.network.ServerAddress;

import java.util.Arrays;
import java.util.List;


@RestController
public class AuthController {

    private static final String VALID_USERNAME = "user";
    private static final String VALID_PASSWORD = "pass";

    private static final List<ServerAddress> serverAddresses = Arrays.asList(
            new ServerAddress("192.168.0.101", 8081),
            new ServerAddress("192.168.0.102", 8082),
            new ServerAddress("localhost", 12345)
    );

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws InterruptedException {
        if (VALID_USERNAME.equals(request.getUsername()) && VALID_PASSWORD.equals(request.getPassword())) {
            //Thread.sleep(5000);
            return ResponseEntity.ok(serverAddresses);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}