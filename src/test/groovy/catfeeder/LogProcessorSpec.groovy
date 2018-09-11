package catfeeder

import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime

@Unroll
class LogProcessorSpec extends Specification {
    @Shared String commonPath = 'src/test/resources/catfeeder/logs/'

    def 'process local log files'(){
        given: "a new output file"
        File outputFile = new File("build/tmp/${logType}.json")
        String output = "["

        when: "you process all of the files"
        LogProcessor logProcessor = new LogProcessor()
        File logFilesDir = new File("${commonPath}${logType}")
        logFilesDir.listFiles().each { File f ->
            LogEntry logEntry = logProcessor.processLogFile(f)
            output += logEntry.json
        }

        and:
        output = output.substring(0, output.lastIndexOf(','))
        output += "\n]"
        FileUtils.write(outputFile, output, StandardCharsets.UTF_8)

        then:
        1==1

        where:
        logType << ['measureFood','measureWater','outputFood','switchServoControl']
    }

    def 'test #type file'(){
        given: "a measureFood file"
        File f = new File("${commonPath}${path}")
        LogEntry expectedLogEntry = new LogEntry(type: type, time: LocalDateTime.parse(time), val: val, unit: unit)

        when: "it's processed"
        LogProcessor processor = new LogProcessor()
        LogEntry actualLogEntry = processor.processLogFile(f)

        then: "it processes correctly"
        assert actualLogEntry.type == expectedLogEntry.type
        assert actualLogEntry.time == expectedLogEntry.time
        assert actualLogEntry.val == expectedLogEntry.val
        assert actualLogEntry.unit == expectedLogEntry.unit

        and: "the json is delightful"
        assert actualLogEntry.json == expectedJson

        where:
        path                                                          | type                 | time                  | val    | unit
        'measureFood/measureFood_2018-04-21_090016.log'               | 'measureFood'        | '2018-04-21T09:00:16' | '3.91' | 'inches'
        'measureWater/measureWater_2018-04-21_230001.log'             | 'measureWater'       | '2018-04-21T23:00:01' | '7.65' | 'inches'
        'outputFood/outputFood_2018-05-13_040003.log'                 | 'outputFood'         | '2018-05-13T04:00:03' | '0.7'  | 'spinDuration'
        'switchServoControl/switchServoControl_2018-04-28_074553.log' | 'switchServoControl' | '2018-04-28T07:45:53' | '0.3'  | 'spinDuration'

        expectedJson << [
            "\n[1524301216000,3.91],", // measureFood
            "\n[1524351601000,7.65],", // measureWater
            "\n{\n" +
            "    \"x\": 1526184003000,\n" +
            "    \"title\": \"M\",\n" +
            "    \"text\": \"Meal\"\n" +
            "},", // outputFood
            "\n{\n" +
            "    \"x\": 1524901553000,\n" +
            "    \"title\": \"S\",\n" +
            "    \"text\": \"Snack\"\n" +
            "},", // switchServoControl
            ]
    }
}
