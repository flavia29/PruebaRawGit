package es.dawequipo3.growing.model;


import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


@Entity
public class Completed_plan {

    public interface Basic{}
    public interface Completed {}

    @EmbeddedId
    private CompletedPlanPK completedPlanPK;

    @JsonView(Basic.class)
    @ManyToOne
    @MapsId("user_PK")
    private User user;

    @JsonView(Completed.class)
    @ManyToOne
    @MapsId("planPK")
    private Plan plan;

    @JsonView(Completed.class)
    @Column(updatable = false)
    private long date;

    public Completed_plan(User user, Plan plan) {
        super();
        this.completedPlanPK = new CompletedPlanPK(user.getEmail(), plan.getName());
        this.user = user;
        this.plan = plan;
        this.date = Calendar.getInstance().getTimeInMillis();
    }

    public Completed_plan() {

    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getPlanName() {
        return plan.getName();
    }

    /**
     * The date is returned in the format:yyyy-MM-dd-HH:mm:ss:SSS
     * @return
     */
    public String getDate() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(this.date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
        return format.format(date.getTime());
    }


}
