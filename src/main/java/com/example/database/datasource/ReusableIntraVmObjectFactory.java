package com.example.database.datasource;

import com.atomikos.util.IntraVmObjectFactory;
import com.atomikos.util.IntraVmObjectRegistry;
import com.atomikos.util.SerializableObjectFactory;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

/***
 *
 */
@Component
public class ReusableIntraVmObjectFactory extends IntraVmObjectFactory {

    private static final String NAME_REF_ADDRESS_TYPE = "uniqueResourceName";

    /**
     *
     * @param object
     * @param name
     * @return
     * @throws NamingException
     */
    public static synchronized Reference createReference(Serializable object, String name) throws NamingException {
        Reference ret = null;
        if (object == null)
            throw new IllegalArgumentException("invalid resource: null");
        if (name == null)
            throw new IllegalArgumentException("name should not be null");

        // make sure that lookup works - add the bean to the registry if needed
        try {
            Object existing = IntraVmObjectRegistry.getResource(name);
            if (existing != object) {
                // another instance with the same name already there
                IntraVmObjectRegistry.removeResource(name);
                IntraVmObjectRegistry.addResource(name, object);
            }
        } catch (NameNotFoundException notThere) {
            // make sure this bean is registered for JNDI lookups to find the
            // same instance
            // otherwise, concurrent lookups would create race conditions during
            // init
            // and the thread that creates a bean might not be able to use it
            // (unfair?)
            IntraVmObjectRegistry.addResource(name, object);
        }
        ret = SerializableObjectFactory.createReference(object);
        // also add the unique resource name for helping during retrieval
        ret.add(new StringRefAddr(NAME_REF_ADDRESS_TYPE, name));
        return ret;
    }
}

