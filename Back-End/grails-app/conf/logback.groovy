import grails.util.Environment
import org.springframework.boot.ApplicationPid
import java.nio.charset.Charset
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import static ch.qos.logback.core.spi.FilterReply.ACCEPT
import static ch.qos.logback.core.spi.FilterReply.DENY
import ch.qos.logback.classic.filter.LevelFilter



def basePath = "C:\\UMM Backend Logs"


//scan("30 seconds")
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


if(!Environment.isDevelopmentMode()) {
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

    logger('grails.app.controllers.com.ef.umm', DEBUG, ['APP_LOG', 'EXC_LOG'], false)
    logger('grails.app.services.com.ef.umm', DEBUG, ['APP_LOG', 'EXC_LOG'], false)
    logger("org.springframework.security", DEBUG, ['SEC_LOG', 'EXC_LOG'], false)
    logger("grails.plugin.springsecurity", TRACE, ['SEC_LOG', 'EXC_LOG'], false)
    logger("org.pac4j", DEBUG, ['SEC_LOG', 'EXC_LOG'], false)
}
else {
    logger('grails.app.controllers.com.ef.umm', DEBUG, ['STDOUT'], false)
    logger("org.springframework.security", TRACE, ['STDOUT'], false)
    logger("grails.plugin.springsecurity", TRACE, ['STDOUT'], false)
    logger("org.pac4j", TRACE, ['STDOUT'], false)
}

root(ERROR, ['STDOUT'])


