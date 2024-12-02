module org.socialSound.socialSound {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
	requires org.mongodb.driver.sync.client;
	requires org.mongodb.driver.core;
	requires org.mongodb.bson;
	requires javafx.graphics;
	

    opens org.socialSound.socialSound to javafx.fxml;
    exports org.socialSound.socialSound;
}
