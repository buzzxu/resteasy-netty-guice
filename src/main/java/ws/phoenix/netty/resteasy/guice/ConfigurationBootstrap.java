package ws.phoenix.netty.resteasy.guice;


import org.jboss.resteasy.spi.ResteasyConfiguration;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.scannotation.ClasspathUrlFinder;

import java.net.URL;
import java.util.*;

/**
 * Created by xux on 14-3-11.
 */
final class ConfigurationBootstrap extends org.jboss.resteasy.plugins.server.servlet.ConfigurationBootstrap {

    private static Object RD_LOCK = new Object();
    private Map<String,String> properties;
    public ConfigurationBootstrap(NettyBootstrap bootstrap){
        this.properties = bootstrap.getProperties();
    }

    @Override
    public ResteasyDeployment createDeployment() {
        synchronized (RD_LOCK){
            ResteasyDeployment deployment = super.createDeployment();
            deployment.getDefaultContextObjects().put(ResteasyConfiguration.class, this);
            return deployment;
        }
    }



    @Override
    public URL[] getScanningUrls() {

        return ClasspathUrlFinder.findClassPaths();
    }

    protected Set<String> getConfPropNames()
    {
        return properties.keySet();
    }
    @Override
    public String getParameter(String name) {
        return properties.get(name);
    }

    @Override
    public Set<String> getParameterNames() {
        return getConfPropNames();
    }

    @Override
    public String getInitParameter(String name) {
        return properties.get(name);
    }

    @Override
    public Set<String> getInitParameterNames() {
        return getParameterNames();
    }


}
