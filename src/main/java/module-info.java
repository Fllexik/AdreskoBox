module sk.bakaj.adreskobox {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens sk.bakaj.adreskobox to javafx.fxml;
    exports sk.bakaj.adreskobox;
}