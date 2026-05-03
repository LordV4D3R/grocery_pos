module fourty3.grocerypos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;

    opens fourty3.grocerypos.controller to javafx.fxml;
    exports fourty3.grocerypos.app;
}