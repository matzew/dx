package io.xn.dx.jaxrs;

import com.google.common.collect.ImmutableSet;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import io.undertow.Undertow;
import io.xn.dx.AvailablePortFinder;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.junit.Test;
import org.skife.jetty.v9.client.HttpClient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@Module(library = true,
        includes = DaggerApplicationDefaults.class)
public class DaggerApplicationTest
{
    @Test
    public void testFoo() throws Exception
    {
        int port = AvailablePortFinder.getNextAvailable(2222);
        UndertowJaxrsServer ut = new UndertowJaxrsServer();

        DaggerApplication app = ObjectGraph.create(this).get(DaggerApplication.class);
        ut.deploy(app);

        ut.start(Undertow.builder().addListener(port, "0.0.0.0"));

        HttpClient http = new HttpClient();
        http.start();

        String c = http.GET(format("http://127.0.0.1:%d/", port)).getContentAsString();
        assertThat(c).isEqualTo("hello dagger");

        http.stop();
        ut.stop();
    }

    @Provides
    @Named("bind-port")
    @Singleton
    public Integer getBindPort()
    {
        return AvailablePortFinder.getNextAvailable();
    }

    @Provides
    @Named("bind-host")
    @Singleton
    public String getBindHost()
    {
        return "0.0.0.0";
    }

    @Provides(type = Provides.Type.SET_VALUES)
    @Resources
    public Set<Object> roots(Root root)
    {
        return ImmutableSet.<Object>of(root);
    }

    @Path("/")
    public static class Root
    {
        @Inject
        public Root(Foo foo) {}

        @GET
        @Produces("text/plain")
        public String get()
        {
            return "hello dagger";
        }
    }

    @Singleton
    public static class Foo
    {
        @Inject
        public Foo() {}
    }
}
