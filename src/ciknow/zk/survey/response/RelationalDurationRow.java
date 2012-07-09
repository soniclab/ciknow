package ciknow.zk.survey.response;

import ciknow.domain.Node;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Spinner;

/**
 *
 * @author gyao
 */
public class RelationalDurationRow extends Row implements IdSpace {

    private static final long serialVersionUID = -3902957071713495757L;
    private static final int YEAR = 60 * 60 * 24 * 31 * 12;
    private static final int MONTH = 60 * 60 * 24 * 31;
    private static final int DAY = 60 * 60 * 24;
    private static final int HOUR = 60 * 60;
    private static final int MINUTE = 60;
    public final Node node;
    private Long value;
    @Wire
    private Label nodeLabel;
    @Wire
    private Spinner yearSpinner;
    @Wire
    private Spinner monthSpinner;
    @Wire
    private Spinner daySpinner;
    @Wire
    private Spinner hourSpinner;
    @Wire
    private Spinner minuteSpinner;
    @Wire
    private Spinner secondSpinner;

    public RelationalDurationRow(Node node, Long value) {
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/response/RelationalDurationRow.zul", this, null);

        Selectors.wireVariables(this, this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);

        this.node = node;
        nodeLabel.setValue(node.getLabel());

        setDurationValue(value);
    }

    private void setDurationValue(Long v) {
        this.value = v;

        Long year = v / YEAR;
        yearSpinner.setValue(year.intValue());

        v = v % YEAR;
        Long month = v / MONTH;
        monthSpinner.setValue(month.intValue());

        v = v % MONTH;
        Long day = v / DAY;
        daySpinner.setValue(day.intValue());

        v = v % DAY;
        Long hour = v / HOUR;
        hourSpinner.setValue(hour.intValue());

        v = v % HOUR;
        Long minute = v / MINUTE;
        minuteSpinner.setValue(minute.intValue());

        Long second = v % MINUTE;
        secondSpinner.setValue(second.intValue());
    }

    public Long getDurationValue() {
        long v = 0;
        v += secondSpinner.getValue();
        v += minuteSpinner.getValue() * MINUTE;
        v += hourSpinner.getValue() * HOUR;
        v += daySpinner.getValue() * DAY;
        v += monthSpinner.getValue() * MONTH;
        v += yearSpinner.getValue() * YEAR;
        value = v;

        return value;
    }
}
