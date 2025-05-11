package config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import Controllers.ReservationController;

/**
 * Configuration JAX-RS pour l'API
 */
@ApplicationPath("/api")
public class JaxRsConfig extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Enregistrer tous les contrôleurs REST
        classes.add(ReservationController.class);
        
        return classes;
    }
} 