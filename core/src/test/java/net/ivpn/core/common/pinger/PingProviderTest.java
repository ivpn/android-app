package net.ivpn.core.common.pinger;

import net.ivpn.core.rest.data.model.Server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PingProviderTest {

    private PingProvider pingProvider;

    @Before
    public void setupPingProvider() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        pingProvider = PingProvider.INSTANCE;
    }

    @Test
    public void pingTwoDifferentServerAndCompare() throws Exception {
        Server server1 = getServerMock();
        Server server2 = getServerMock();

        assertNotEquals(server1, server2);

        final CountDownLatch lock = new CountDownLatch(2);

        final PingResultFormatter[] status1 = new PingResultFormatter[1];
        final PingResultFormatter[] status2 = new PingResultFormatter[1];

        pingProvider.ping(server1, new OnPingFinishListener() {
            @Override
            public void onPingFinish(PingResultFormatter status) {
                status1[0] = status;
                lock.countDown();
            }
        });

        pingProvider.ping(server2, new OnPingFinishListener() {
            @Override
            public void onPingFinish(PingResultFormatter status) {
                status2[0] = status;
                lock.countDown();
            }
        });

        lock.await(3000, TimeUnit.MILLISECONDS);

        assertNotNull(status1[0]);
        assertNotNull(status2[0]);

        assertNotEquals(status1[0], status2[0]);
    }

    @Test
    public void pingOneServer() throws Exception {
        Server server1 = getServerMock();

        final CountDownLatch lock = new CountDownLatch(1);

        final PingResultFormatter[] status1 = new PingResultFormatter[1];
        final PingResultFormatter[] status2 = new PingResultFormatter[1];

        pingProvider.ping(server1, new OnPingFinishListener() {
            @Override
            public void onPingFinish(PingResultFormatter status) {
                status1[0] = status;
                lock.countDown();
            }
        });

        lock.await(3000, TimeUnit.MILLISECONDS);

        assertNotNull(status1);
        final CountDownLatch lockS = new CountDownLatch(1);

        pingProvider.ping(server1, new OnPingFinishListener() {
            @Override
            public void onPingFinish(PingResultFormatter status) {
                status2[0] = status;
                lockS.countDown();
            }
        });

        lockS.await(100, TimeUnit.MILLISECONDS);

        assertNotNull(status2[0]);

        assertEquals(status1[0], status2[0]);
    }

    @Test(timeout = 5000)
    public void pingServerAndReturnToCorrect() throws Exception {
        Server server1 = getServerMock();

        final CountDownLatch lock = new CountDownLatch(1);

        final PingResultFormatter[] status1 = new PingResultFormatter[1];
        final PingResultFormatter[] status2 = new PingResultFormatter[1];

        pingProvider.ping(server1, new OnPingFinishListener() {
            @Override
            public void onPingFinish(PingResultFormatter status) {
                status1[0] = status;
                lock.countDown();
            }
        });

        pingProvider.ping(server1, new OnPingFinishListener() {
            @Override
            public void onPingFinish(PingResultFormatter status) {
                status2[0] = status;
                lock.countDown();
            }
        });

        lock.await(3000, TimeUnit.MILLISECONDS);

        assertNull(status1[0]);
        assertNotNull(status2[0]);
    }

    private Server getServerMock() {
        Server server = mock(Server.class);
        when(server.getGateway()).thenReturn("fr.gw.ivpn.net");
        return server;
    }

}