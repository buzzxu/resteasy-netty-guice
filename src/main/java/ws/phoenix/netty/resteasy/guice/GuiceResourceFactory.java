package ws.phoenix.netty.resteasy.guice;

import com.google.inject.Provider;
import org.jboss.resteasy.spi.*;

/**
 * Created by 徐翔 on 14-3-12.
 */
public final class GuiceResourceFactory implements ResourceFactory
{

    private final Provider provider;
    private final Class<?> scannableClass;
    private PropertyInjector propertyInjector;

    public GuiceResourceFactory(final Provider provider, final Class<?> scannableClass)
    {
        this.provider = provider;
        this.scannableClass = scannableClass;
    }

    public Class<?> getScannableClass()
    {
        return scannableClass;
    }

    public void registered(ResteasyProviderFactory factory)
    {
        propertyInjector = factory.getInjectorFactory().createPropertyInjector(scannableClass, factory);
    }

    public Object createResource(final HttpRequest request, final HttpResponse response, final ResteasyProviderFactory factory)
    {
        final Object resource = provider.get();
        propertyInjector.inject(request, response, resource);
        return resource;
    }

    public void requestFinished(final HttpRequest request, final HttpResponse response, final Object resource)
    {
    }

    public void unregistered()
    {
    }
}
