import static ch.qos.logback.classic.Level.*
import ch.qos.logback.classic.encoder.*
import ch.qos.logback.classic.filter.*
import ch.qos.logback.core.*
import ch.qos.logback.core.status.OnConsoleStatusListener
 
statusListener(OnConsoleStatusListener)
 
appender("STDOUT", ConsoleAppender) {
  filter(ThresholdFilter) {
    level = INFO
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

root INFO, ['STDOUT']
logger "org.springframework", ERROR