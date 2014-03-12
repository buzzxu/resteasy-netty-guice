package ws.phoenix.netty.resteasy.guice;

import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.name.Names;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.spi.ResteasyDeployment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import com.google.inject.Module;

import javax.annotation.PostConstruct;

/**
 * Created by xux on 14-3-12.
 */
public class NettyBootstrap {
    private static Logger logger = Logger.getLogger(NettyBootstrap.class);
    private Injector injector;
    Properties confProp;
    private List<Module> modules = Lists.newArrayList();
    private ResteasyDeployment deployment;

    public NettyBootstrap(){
        //load netty.properties
        loadProp();
    }

    public NettyBootstrap createInjector(Module module){
        modules.add(module);
        return createInjector();
    }
    public NettyBootstrap createInjector(){
        modules.add(new GuiceModule(confProp,deployment));
        final Stage stage = getStage();
        if (stage == null)
        {
            injector = Guice.createInjector(modules);
        }
        else
        {
            injector = Guice.createInjector(stage, modules);
        }
        ModuleProcessor processor = new ModuleProcessor(deployment.getRegistry(),deployment.getProviderFactory());
        processor.processInjector(injector);

        while (injector.getParent() != null) {
            injector = injector.getParent();
            processor.processInjector(injector);
        }
        triggerAnnotatedMethods(PostConstruct.class);

        return this;
    }
    public NettyBootstrap  createDeployment(){
       return createDeployment(null);
    }
    public NettyBootstrap  createDeployment(ResteasyDeployment rd){
        if(rd == null){
            deployment = new ResteasyDeployment();
        }else{
            deployment = rd;
        }
        deployment.start();
        return this;
    }





    private void triggerAnnotatedMethods(final Class<? extends Annotation> annotationClass)
    {
        for (Module module : this.modules)
        {
            final Method[] methods = module.getClass().getMethods();
            for (Method method : methods)
            {
                if (method.isAnnotationPresent(annotationClass))
                {
                    if(method.getParameterTypes().length > 0)
                    {
                        logger.warn("Cannot execute expected module {}'s @{} method {} because it has unexpected parameters: skipping.", module.getClass().getSimpleName(), annotationClass.getSimpleName(), method.getName());
                        continue;
                    }
                    try
                    {
                        method.invoke(module);
                    } catch (InvocationTargetException ex) {
                        logger.warn("Problem running annotation method @" + annotationClass.getSimpleName(), ex);
                    } catch (IllegalAccessException ex) {
                        logger.warn("Problem running annotation method @" + annotationClass.getSimpleName(), ex);
                    }
                }
            }
        }
    }
    protected void loadProp(){
        confProp = new Properties();
        InputStream in = null;
        try {
            in = NettyBootstrap.class.getResourceAsStream("/netty.properties");
            confProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    protected Stage getStage()
    {
        final String stageAsString = confProp.getProperty("resteasy.guice.stage");
        if (stageAsString == null)
        {
            return null;
        }
        try
        {
            return Stage.valueOf(stageAsString.trim());
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Injector stage is not defined properly. " + stageAsString + " is wrong value." +
                    " Possible values are PRODUCTION, DEVELOPMENT, TOOL.");
        }
    }


    public void start(SecurityDomain domain){
        GuiceNettyJaxrsServer server = injector.getInstance(GuiceNettyJaxrsServer.class);
        server.setSecurityDomain(domain);
        server.start();

    }
    public void start(){
        start(null);
    }

    public void stop(){
        injector.getInstance(ResteasyDeployment.class).stop();
        injector.getInstance(GuiceNettyJaxrsServer.class).stop();
    }

}
