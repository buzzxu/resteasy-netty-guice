package ws.phoenix.netty.resteasy.guice;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.io.IOException;

/**
 * Created by xux on 14-3-11.
 */
public final class GuiceRequestDispatcher extends RequestDispatcher {
    public GuiceRequestDispatcher(SynchronousDispatcher dispatcher, ResteasyProviderFactory providerFactory, SecurityDomain domain) {
        super(dispatcher, providerFactory, domain);
    }


    @Override
    public void service(HttpRequest request, HttpResponse response, boolean handleNotFound) throws IOException {
        super.service(request, response, handleNotFound);
    }
}
