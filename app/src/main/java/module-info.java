module easyrelease.cli.app.main {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires jdk.compiler;

    exports com.sqdstudios.easyrelease.cli.model;
}