package acl.dao;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import acl.model.AccessPolicy;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * {@link Objectify} configuration.
 * 
 * @author Petr Giecek
 */
@Component
public class ObjectifyConfig {

    static {
        
        // register all persistent entities
        ObjectifyService.register(AccessPolicy.class);
        ObjectifyService.register(AccessPolicy.SecurityIdentity.class);

    }
    
    /**
     * Returns the current {@link Objectify} instance.
     * 
     * @return the current {@link Objectify} instance
     */
    @Bean
    @Scope(value=BeanDefinition.SCOPE_PROTOTYPE, proxyMode=ScopedProxyMode.INTERFACES)
    public Objectify objectify() {
        return ObjectifyService.ofy();
    }

}
