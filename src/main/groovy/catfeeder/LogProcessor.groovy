package catfeeder

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import org.apache.commons.io.FileUtils

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LogProcessor {
    static final String websiteBucket = "catfeeder-website"

    // https://www.polyglotdeveloper.com/lambda/2017-07-05-Using-Lambda-as-S3-events-processor/#create-lambda-function
    String handleRequest(S3Event newLogEntryS3event, Context context) {
        try {
            S3Object newLogEntryS3Object = getNewLogEntryS3Object(newLogEntryS3event)
            LogEntry logEntry = getLogEntry(newLogEntryS3Object)
            S3Object websiteJsonS3Object = getWebsiteJsonS3Object(logEntry)
            File websiteJsonFile = getWebsiteJsonFile(websiteJsonS3Object)
            updateWebsiteJsonFile(websiteJsonFile, logEntry)
            putWebsiteJsonFile(websiteJsonS3Object, websiteJsonFile)
            deleteNewLogEntryS3Object(newLogEntryS3Object)
            println "Successfully processed!: ${newLogEntryS3Object.bucketName}/${newLogEntryS3Object.key}"
            return "Ok"
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    S3Object getNewLogEntryS3Object(S3Event newLogEntryS3event){
        println "getNewLogEntryS3Object..."
        // STEP1: Read input event and extract file details which got added to the source bucket
        S3EventNotification.S3EventNotificationRecord record = newLogEntryS3event.getRecords().get(0)
        String srcBucket = record.getS3().getBucket().getName()
        // Remove any spaces or unicode non-ASCII characters.
        String srcKey = record.getS3().getObject().getKey().replace('+', ' ')
        srcKey = URLDecoder.decode(srcKey, "UTF-8")
        String dstKey = srcKey
        srcKey = srcKey.replace(" ", "")
        println "srcBucket:${srcBucket}"
        println "srcKey:${srcKey}"

        // STEP2: Create S3 client and read the object as a stream
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient()
        s3Client.getObject(new GetObjectRequest(srcBucket, srcKey))
    }

    LogEntry getLogEntry(S3Object newLogEntryS3Object){
        println "getLogEntry..."
        //write to tmp: https://forums.aws.amazon.com/thread.jspa?threadID=174119
        File tempLogEntryFile = new File("/tmp/${newLogEntryS3Object.key}")
        println "tempLogEntryFile: ${tempLogEntryFile}"
        FileUtils.write(tempLogEntryFile, newLogEntryS3Object.getObjectContent().text, StandardCharsets.UTF_8)
        processLogFile(tempLogEntryFile)
    }

    LogEntry processLogFile(File f) {
        println "processLogFile..."
        int underscoreLoc = f.name.indexOf('_')
        String type = f.name.substring(0, underscoreLoc)

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")
        LocalDateTime time = LocalDateTime.parse(f.name.substring(underscoreLoc+1, f.name.indexOf('.')), formatter)

        String val
        String unit
        String contents = f.text
        if(contents.contains(':')){
            // a servo entry
            val = contents.substring(contents.indexOf(' ')+1)
            unit = contents.substring(0, contents.indexOf(':'))
        } else {
            // a measure entry
            val = contents.substring(0, contents.indexOf(' '))
            Double dVal = Double.parseDouble(val)
            unit = contents.substring(contents.indexOf(' ')+1)

            // determine if we should alert to logs (cloud watch will monitor for this and do emails)
            String alertType = 'Water'
            Double minVal = Double.parseDouble(System.getenv('WATER_ALERT_MIN_VAL') ?: '1.1')
            Double maxVal = Double.parseDouble(System.getenv('WATER_ALERT_MAX_VAL') ?: '9')
            if(type.toLowerCase().contains('food')){
                alertType = 'Food'
                minVal = Double.parseDouble(System.getenv('FOOD_ALERT_MIN_VAL') ?: '1.6')
                maxVal = Double.parseDouble(System.getenv('FOOD_ALERT_MAX_VAL') ?: '9')
            }

            if(dVal >= minVal && dVal <= maxVal){
                println "${alertType} level is too low! Measurement value: ${dVal.toString()} " +
                        "minVal: ${minVal} maxVal: ${maxVal}"
            }

            val = dVal.toString()
        }

        LogEntry logEntry = new LogEntry(type: type, time: time, val: val, unit: unit)
        println "logEntry: ${logEntry.toString()}"
        logEntry
    }

    S3Object getWebsiteJsonS3Object(LogEntry logEntry){
        println "getWebsiteJsonS3Object..."
        String websiteJsonFileName = "${logEntry.type}.json"
        println "websiteJsonFileName: ${websiteJsonFileName}"
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient()
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(websiteBucket, websiteJsonFileName))
        s3Object
    }

    File getWebsiteJsonFile(S3Object newLogEntryS3Object){
        println "getWebsiteJsonFile..."
        //write to tmp: https://forums.aws.amazon.com/thread.jspa?threadID=174119
        File websiteJsonFile = new File("/tmp/${newLogEntryS3Object.key}")
        println "websiteJsonFile: ${websiteJsonFile}"
        FileUtils.write(websiteJsonFile, newLogEntryS3Object.getObjectContent().text, StandardCharsets.UTF_8)
        websiteJsonFile
    }

    void updateWebsiteJsonFile(File websiteJsonFile, LogEntry logEntry){
        println "updateWebsiteJsonFile..."
        String contents = FileUtils.readFileToString(websiteJsonFile, StandardCharsets.UTF_8)
        contents = "${contents.substring(0, contents.lastIndexOf(']'))}${logEntry.getJson()}]"
        FileUtils.write(websiteJsonFile, contents, StandardCharsets.UTF_8)
    }

    void putWebsiteJsonFile(S3Object websiteJsonS3Object, File websiteJsonFile){
        println "putWebsiteJsonFile..."
        //NOTE: Concurrency is set to 1 in the lambda function settings so you should be able to overwrite the file without worry
        // STEP4: Uploading to S3 target bucket
        println "Writing to: ${websiteBucket}/${websiteJsonS3Object.key}"
        ObjectMetadata meta = new ObjectMetadata()
        meta.setContentType(websiteJsonS3Object.getObjectMetadata().getContentType())
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient()
        s3Client.putObject(websiteBucket, websiteJsonS3Object.key, websiteJsonFile.newDataInputStream(), null)
    }

    void deleteNewLogEntryS3Object(S3Object newLogEntryS3Object){
        println "deleteNewLogEntryS3Object..."
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient()
        s3Client.deleteObject(newLogEntryS3Object.bucketName, newLogEntryS3Object.key)
    }
}
