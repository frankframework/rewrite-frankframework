package org.frankframework.pipes;


import org.frankframework.configuration.ConfigurationWarning;

public class MySecondPipe {
    private String myPipeAttribute;

    @Deprecated
    @ConfigurationWarning("Change to myPipeAttribute")
    public void setPipeAttribute(String pipeAttribute) {
        this.myPipeAttribute = pipeAttribute;
    }

    public void setMyPipeAttribute(String pipeAttribute) {
        this.myPipeAttribute = pipeAttribute;
    }
}
