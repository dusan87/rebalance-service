  # Rebalancing Service

  ## Start service

  Start service with `Demo` profile in order to point to Fake FPS

    ```sh
    mvn spring-boot:run -Dspring-boot.run.profiles=Demo
    ```

  ### Environment configuration

  Under `application.properties` we can configure batch size for trades to be sent to FPS

  ```
  financial.portfolio.service.api.bach.size=2
  ```
  In addition, we can configure FPS base url and rebalance daily cron

  ```
  financial.portfolio.service.baseUrl=http://localhost:8080
  rebalance.job.cron=*/60 * * * * * # every minute
  ```

  ## Tech Notes

  * FPS service is faked using MockServer to ensure working contract/integration when service available.
  * Testing strategy -> unit-tests + one integration test (happy scenario)
  * For simplicity, I used @Scheduler to schedule daily rebalancing job. It's setup for every 1min to test it out.
  * There are some logs printed simulating the steps being exucted


