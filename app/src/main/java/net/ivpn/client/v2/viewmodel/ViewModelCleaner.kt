package net.ivpn.client.v2.viewmodel

import net.ivpn.client.IVPNApplication
import net.ivpn.client.ui.protocol.ProtocolViewModel
import net.ivpn.client.v2.network.NetworkViewModel
import javax.inject.Inject

class ViewModelCleaner {

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    @Inject
    lateinit var alwaysOnVPN: AlwaysOnVPNViewModel

    @Inject
    lateinit var connect: ConnectionViewModel

    @Inject
    lateinit var killswitch: KillSwitchViewModel

    @Inject
    lateinit var location: LocationViewModel

    @Inject
    lateinit var logging: LoggingViewModel

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var filter: ServerListFilterViewModel

    @Inject
    lateinit var serverList: ServerListViewModel

    @Inject
    lateinit var servers: ServersViewModel

    @Inject
    lateinit var startOnBoot: StartOnBootViewModel

    @Inject
    lateinit var protocol: ProtocolViewModel

    @Inject
    lateinit var network: NetworkViewModel

    @Inject
    lateinit var updates: UpdatesViewModel

    @Inject
    lateinit var signUp: SignUpViewModel

    init {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)

        account.reset()
        antiTracker.reset()
        alwaysOnVPN.reset()
        connect.reset()
        killswitch.reset()
        location.reset()
        logging.reset()
        multihop.reset()
        filter.reset()
        serverList.reset()
        servers.reset()
        startOnBoot.reset()
        protocol.reset()
        updates.reset()
        signUp.reset()
        network.reset()
    }
}