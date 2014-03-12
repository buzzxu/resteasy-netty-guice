package ws.phoenix.netty.resteasy.guice;

import org.jboss.resteasy.plugins.server.servlet.ConfigurationBootstrap;
import org.jboss.resteasy.spi.ResteasyConfiguration;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.scannotation.ClasspathUrlFinder;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by xux on 14-3-11.
 */
final class ListenerBootstrap extends ConfigurationBootstrap {

    private static Object RD_LOCK = new Object();
    private NettyBootstrap bootstrap;
    private Properties nettyConf;
    public ListenerBootstrap(NettyBootstrap bootstrap){
        this.bootstrap = bootstrap;
        this.nettyConf = bootstrap.confProp;
    }

    @Override
    public ResteasyDeployment createDeployment() {
        ResteasyDeployment deployment = super.createDeployment();
        deployment.getDefaultContextObjects().put(ResteasyConfiguration.class, this);
        return deployment;
    }



    @Override
    public URL[] getScanningUrls() {

        return ClasspathUrlFinder.findClassPaths();
    }

    protected Set<String> getConfPropNames()
    {
        Enumeration<String> en = (Enumeration<String>) nettyConf.propertyNames();
        HashSet<String> set = new HashSet<String>();
        while (en.hasMoreElements()) set.add(en.nextElement());
        return set;
    }
    @Override
    public String getParameter(String name) {
        return nettyConf.getProperty(name);
    }

    @Override
    public Set<String> getParameterNames() {
        return getConfPropNames();
    }

    @Override
    public String getInitParameter(String name) {
        return nettyConf.getProperty(name);
    }

    @Override
    public Set<String> getInitParameterNames() {
        return getParameterNames();
    }


}
