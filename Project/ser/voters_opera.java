import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

public class voters_opera {
    private String voterinfo;
    private boolean voted;
    private String voted_date;

    public voters_opera(String voterinfoIn) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        this.voterinfo = voterinfoIn;
        this.voted = false;
        this.voted_date = dateFormat.format(date);
    }

    public String voter_info() {
        return this.voterinfo;
    }

    public String voter_id() {
        String[] Id ;
        Id= this.voterinfo.split(" ");
        return Id[1];
    }

    public boolean has_voted() {
        return this.voted;
    }

    public String voted_time() {
        return this.voted_date;
    }

    public void is_voted() {
        this.voted = true;
    }

    public void votedres(String voteTime) {
        this.voted_date = voteTime;
    }
}
