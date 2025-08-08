module org.example.myjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.myjavafx to javafx.fxml;
    exports org.example.myjavafx;
}
