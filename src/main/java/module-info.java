module illumination.jeudelavie {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens illumination.jeudelavie to javafx.fxml;
    exports illumination.jeudelavie;
}