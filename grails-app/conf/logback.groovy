import grails.util.Environment
import org.springframework.boot.ApplicationPid
import java.nio.charset.Charset
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import static ch.qos.logback.core.spi.FilterReply.ACCEPT
import static ch.qos.logback.core.spi.FilterReply.DENY
import ch.qos.logback.classic.filter.LevelFilter


def basePath
if (Environment.isDevelopmentMode()){
    basePath = "C:\\ummLogs-development"
}
else{
    basePath = "C:\\UMM Backend Logs"
}

scan("30 seconds")
// Log information about the configuration.
statusListener(OnConsoleStatusListener)

if (!System.getProperty("PID")) {
    System.setProperty("PID", (new ApplicationPid()).toString())
}

conversionRule 'clr', org.springframework.boot.logging.logback.ColorConverter
conversionRule 'wex', org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(%property{PID}){magenta} ' + // PID
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

appender("APP_LOG", RollingFileAppender) {

    append = true
    encoder(PatternLayoutEncoder) {
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                '%clr(%5p) ' + // Log level
                '%clr(%property{PID}){magenta} ' + // PID
                '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                '%m%n%wex' // Message
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${basePath}\\Application\\application-%d{yyyy-MM-dd}.log"
        maxHistory = 30
        totalSizeCap = FileSize.valueOf("5MB")
    }
    filter(ch.qos.logback.classic.filter.LevelFilter) {
        level = ERROR
        onMatch = DENY
        onMismatch = ACCEPT
    }
}

appender("SEC_LOG", RollingFileAppender) {

    append = true
    encoder(PatternLayoutEncoder) {
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(%property{PID}){magenta} ' + // PID
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${basePath}\\Security\\security-%d{yyyy-MM-dd}.log"
        maxHistory = 30
        totalSizeCap = FileSize.valueOf("5MB")
    }
}

appender("EXC_LOG", RollingFileAppender) {

    append = true
    encoder(PatternLayoutEncoder) {
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(%property{PID}){magenta} ' + // PID
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${basePath}\\Exceptions\\exception-%d{yyyy-MM-dd}.log"
        maxHistory = 30
        totalSizeCap = FileSize.valueOf("5MB")
    }

    filter(ch.qos.logback.classic.filter.ThresholdFilter) {
        level = ERROR
    }
}

root(ERROR, ['STDOUT'])
//root(INFO, ['FULL_LOG', 'STDOUT', 'EXC_LOG'])
logger('grails.app.controllers.com.ef.umm', DEBUG, ['APP_LOG', 'EXC_LOG'], false)
logger("org.springframework.security", INFO, ['SEC_LOG', 'EXC_LOG'], false)
logger("grails.plugin.springsecurity", INFO, ['SEC_LOG', 'EXC_LOG'], false)
logger("org.pac4j", INFO, ['SEC_LOG', 'EXC_LOG'], false)