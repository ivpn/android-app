package net.ivpn.core.common.pinger

sealed class ResultPing

class Success constructor(val stats: PingStats): ResultPing()
class Error constructor(val error: Throwable?): ResultPing()
