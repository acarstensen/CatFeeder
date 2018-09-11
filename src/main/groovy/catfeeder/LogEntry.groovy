package catfeeder

import java.time.LocalDateTime
import java.time.ZoneOffset

class LogEntry {
    String type
    LocalDateTime time
    String val
    String unit

    String getJson(){
        String json
        String timeMilli = time.toInstant(ZoneOffset.UTC).toEpochMilli()
        if(type.startsWith('measure')){
            json = "\n[${timeMilli},${val}],"
        } else if(type.contains('Food')) {
            json = "\n{\n" +
                    "    \"x\": ${timeMilli},\n" +
                    "    \"title\": \"M\",\n" +
                    "    \"text\": \"Meal\"\n" +
                    "},"
        } else if(type.contains('switch')) {
            json = "\n{\n" +
                    "    \"x\": ${timeMilli},\n" +
                    "    \"title\": \"S\",\n" +
                    "    \"text\": \"Snack\"\n" +
                    "},"
        }

        json
    }
}
