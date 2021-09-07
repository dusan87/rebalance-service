package com.vanguard.portfolio.rebalancing.job

import com.vanguard.portfolio.rebalancing.services.Rebalancer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RebalanceJobScheduler(
    private val service: Rebalancer
) {
    private var log: Logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${rebalance.job.cron}")
    fun run() {
        log.info("****** Start Daily Rebalancing ********")

        service.rebalance()

        log.info("******** Finished Daily Rebalancing ********")
    }
}
