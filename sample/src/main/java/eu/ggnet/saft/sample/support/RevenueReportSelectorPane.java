package eu.ggnet.saft.sample.support;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.ResultProducer;
import eu.ggnet.saft.core.ui.Title;

/**
 * Shows a selector pane for the Revenue Report.
 * <p>
 * @author oliver.guenther
 */
@Title("Umsatzreport Parameter")
public class RevenueReportSelectorPane extends GridPane implements ResultProducer<RevenueReportSelectorPane> {

    private final ObjectProperty<Step> step = new SimpleObjectProperty<>();

    private final ObjectProperty<Category> category = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDate> start;

    private final ObjectProperty<LocalDate> end;

    private boolean ok = false;

    public RevenueReportSelectorPane() {
        setAlignment(Pos.CENTER);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(25, 25, 25, 25));
        ChoiceBox<Step> stepChoice = new ChoiceBox<>();
        stepChoice.getItems().addAll(Step.values());
        step.bind(stepChoice.getSelectionModel().selectedItemProperty());
        stepChoice.getSelectionModel().select(Step.DAY);

        addRow(0, new Label("Step:"), stepChoice);

        ChoiceBox<Category> contractorChoice = new ChoiceBox<>();
        contractorChoice.getItems().addAll(Category.values());

        category.bind(contractorChoice.getSelectionModel().selectedItemProperty());
        contractorChoice.getSelectionModel().selectFirst();
        addRow(1, new Label("Contractor:"), contractorChoice);

        DatePicker startPicker = new DatePicker(LocalDate.of(2014, 01, 01));
        start = startPicker.valueProperty();

        DatePicker endPicker = new DatePicker(LocalDate.of(2014, 12, 31));
        end = endPicker.valueProperty();

        addRow(2, new Label("Start:"), startPicker);
        addRow(3, new Label("End:"), endPicker);

        Button okButton = new Button("Ok");
        okButton.setOnAction(e -> {
            ok = true;
            Ui.closeWindowOf(this);
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            Ui.closeWindowOf(this);
        });

        addRow(4, okButton, cancelButton);
    }

    public Step getStep() {
        return step.get();
    }

    public Category getCategory() {
        return category.get();
    }

    public Date getStart() {
        return Date.from(start.get().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date getEnd() {
        return Date.from(end.get().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public String toString() {
        return "RevenueReportSelectorPane{" + "step=" + step.get() + ", category=" + category.get() + ", start=" + start.get() + ", end=" + end.get() + '}';
    }

    @Override
    public RevenueReportSelectorPane getResult() {
        if ( ok ) return this;
        return null;
    }

}
