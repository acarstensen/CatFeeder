package catfeeder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LogProcessor {

    LogEntry processLogFile(File f) {
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
            unit = contents.substring(contents.indexOf(' ')+1)

            // get rid of bad measurements... there's only 9 inches of space for food and water
            double dVal = Double.parseDouble(val)
            if(dVal > 9){
                dVal = 9
            }
            val = dVal.toString()
        }

        new LogEntry(type: type, time: time, val: val, unit: unit)
    }

}
