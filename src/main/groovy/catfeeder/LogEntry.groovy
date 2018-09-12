package catfeeder

import java.time.LocalDateTime
import java.time.ZoneOffset

class LogEntry {
    String type
    LocalDateTime time
    String val
    String unit

    String getJson(boolean prePendComma = true){
        String json
        String timeMilli = time.toInstant(ZoneOffset.UTC).toEpochMilli()
        if(type.startsWith('measure')){
            json = "[${timeMilli},${val}]"
        } else if(type.contains('Food')) {
            json = "{\n" +
                    "    \"x\": ${timeMilli},\n" +
                    "    \"title\": \"M\",\n" +
                    "    \"text\": \"Meal\"\n" +
                    "}"
        } else if(type.contains('switch')) {
            json = "{\n" +
                    "    \"x\": ${timeMilli},\n" +
                    "    \"title\": \"S\",\n" +
                    "    \"text\": \"Snack\"\n" +
                    "}"
        }

        if(prePendComma){
            json = ",\n${json}"
        } else {
            json = "${json},\n"
        }

        json
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "type='" + type + '\'' +
                ", time=" + time +
                ", val='" + val + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
