package com.Huduk.P2PFS.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController     
public class StatusController {
    
    @GetMapping("/status")
    public String getStatus() {
        return "P2PFS operational";
    }
}
