package net.ivpn.client.common.dagger;

import net.ivpn.client.rest.HttpClientFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    @ApplicationScope
    @Provides
    public HttpClientFactory provideFactory() {
        return new HttpClientFactory();
    }
}
