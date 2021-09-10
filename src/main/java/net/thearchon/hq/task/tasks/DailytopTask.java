package net.thearchon.hq.task.tasks;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.result.DeleteResult;
import net.thearchon.hq.Archon;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class DailytopTask implements Runnable {

    private final Archon archon;

    private int lastHour;
    private DateFormat dateFormat;
    
    public DailytopTask(Archon archon) {
        this.archon = archon;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    @Override
    public void run() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (lastHour == 23 && hour == 0) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            archon.getDataSource().getCollection("dailytop")
                    .deleteMany(new Document(), new SingleResultCallback<DeleteResult>() {
                @Override
                public void onResult(DeleteResult deleteResult, Throwable throwable) {
                    archon.runTask(() -> {
                        archon.getDataSource().execute("INSERT INTO dailytop VALUES(?, ?, ?, ?);",
                                dateFormat.format(cal.getTime()),
                                archon.getCache().getCurrentNewPlayers(),
                                archon.getCache().getCurrentMostOnline(),
                                deleteResult.getDeletedCount());
                        archon.getCache().setCurrentMostOnline(0);
                        archon.getCache().setCurrentNewPlayers(0);
                    });
                }
            });
//            archon.getDataSource().getCollection("dailytop")
//                    .deleteMany(new Document(), (result, t) -> archon.runTask(() -> {
//                        archon.getDataSource().execute("INSERT INTO dailytop VALUES(?, ?, ?, ?);",
//                                dateFormat.format(cal.getTime()),
//                                archon.getCache().getCurrentNewPlayers(),
//                                archon.getCache().getCurrentMostOnline(),
//                                result.getDeletedCount());
//                        archon.getCache().setCurrentMostOnline(0);
//                        archon.getCache().setCurrentNewPlayers(0);
//                    }));
        }
        lastHour = hour;
    }
}
