module com.api.api {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires io.netty.all;

    opens com.api.api to javafx.fxml;
    exports com.api.api;
}