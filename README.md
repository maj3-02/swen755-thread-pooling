# Thread pooling

Requires Java 21. Set `JAVA_HOME` to your JDK 21 installation.
The Gradle wrapper downloads Gradle and dependencies on the first run.

Run the application on Windows:

```powershell
.\gradlew.bat :app:run
```

On macOS or Linux:

```sh
bash ./gradlew :app:run
```

Build or run tests:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

On macOS or Linux, use `bash ./gradlew` instead of `.\gradlew.bat`.

`PrimeSearch.findPrimes(startInclusive, endInclusive)` returns the primes in a
range as an ascending list, including both endpoints. Each call has independent
results and can run concurrently.

`PrimeSearch.findPrimes(startInclusive, endInclusive, threadCount)` uses the
custom fixed-size `ThreadPool`. The candidate range is divided into one
contiguous task per worker. Task sizes differ by at most one candidate, with
any remainder assigned to the first workers. Results are joined in range order,
so the returned primes remain sorted.

`ThreadPool` delegates worker selection to a strategy in the
`threadpooling.schedule` package. `RoundRobinSchedulingStrategy` is used by
default and assigns submitted tasks evenly by task count. A different strategy can be passed to
`ThreadPool(threadCount, strategy)`; each scheduling decision receives an
immutable snapshot of the workers' queued-task counts. The pool finishes queued
work and joins its worker threads when closed. Tests use JUnit 4.13.2.

An alternate `LeastLoadedSchedulingStrategy` is provided as a comparison to `RoundRobinSchedulingStrategy`.
Instead of rotating through each worker in round robin, least-loaded selects the worker with the minimum number
of tasks remaining in the queue. Running these strategies side-by-side demonstrates a slight edge for the
`LeastLoadedSchedulingStrategy` with the current number of threads/tasks:
```bash
Strategy: Round Robin
Threads: 10
Tasks: 100
Prime numbers found: 664579
Execution time: 2.455606 seconds

Strategy: Least Loaded
Threads: 10
Tasks: 100
Prime numbers found: 664579
Execution time: 2.070694 seconds
```
