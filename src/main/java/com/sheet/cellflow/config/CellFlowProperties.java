package com.sheet.cellflow.config;

import com.sheet.cellflow.sheet.service.CircuitBreaker;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class CellFlowProperties {
    
    private int maxLookupChain;

    public int getMaxLookupChain() {
        return maxLookupChain;
    }

    public void setMaxLookupChain(int maxLookupChain) {
        this.maxLookupChain = maxLookupChain;
    }


    @Bean
    public CircuitBreaker circuitBreaker() {
        return new CircuitBreaker();
    }
}