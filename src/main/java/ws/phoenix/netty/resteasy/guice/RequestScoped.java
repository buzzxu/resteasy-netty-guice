package ws.phoenix.netty.resteasy.guice;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 徐翔 on 14-3-12.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestScoped
{
}

